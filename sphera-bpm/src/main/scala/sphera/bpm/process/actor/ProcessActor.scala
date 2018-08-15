package sphera.bpm.process.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.process.Operation.ActiveOperations
import sphera.bpm.process.Process._
import sphera.bpm.process.{ Operation, Process, ProcessDef, ProcessTypes, StartOperationDef, UpdateProcessInfo }
import sphera.bpm.project.Project
import sphera.bpm.runnable.{ RunnableBehavior, RunnableState }
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.bpm.{ Bpm, OperationNotFoundException }

import scala.concurrent.ExecutionContext

class ProcessActor(
  val id: Process.Id,
  val projectId: Project.Id,
  val bpm: Bpm,
  val initState: ProcessState)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableBehavior
  with OperationBuilder
  with ProcessTypes {

  def historyManager = bpm.processHistoryManager

  def start(state: ProcessState): Unit = self ! ActivateCmd(List(state.startOp), None)

  def activate(state: ProcessState, to: List[Operation.BpmnId], from: Option[Operation.BpmnId] = None): Unit = {
    val x1 = to.map { op => context.child(op.raw).getOrElse(throw OperationNotFoundException(op)) }
    persist(ActivatedEvt(to, from)) { event =>
      changeState(state.updated(event))
      publish(event)
      x1 foreach (_ ! Operation.ActivateCmd(from))
    }
  }

  override def afterRecover(state: ProcessState): Unit = {
    // build all operations and send operationIds to manager
    bpm.updateProcess(UpdateProcessInfo(
      id = id,
      operationIds = Some(buildOperations(state.processDef.opDefs))))

    // run activation after recover
    val activeOperations = state.activeOperations.flatMap(op => context.child(op.raw))
    activeOperations.foreach(_ ! Operation.ActivateCmd(None))

    log.info("operations created")
  }

  def behavior(state: ProcessState): Receive = {
    case ActivateCmd(x, y) => activate(state, x, y)
  }
}

object ProcessActor {
  def props(
    processId: Process.Id,
    projectId: Project.Id,
    bpm: Bpm,
    state: ProcessState = ProcessState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ProcessActor(
      id = processId,
      projectId = projectId,
      bpm = bpm,
      initState = state))
  }
}

case class ProcessState(processDef: ProcessDef, storage: RunnableStorage, activeOperations: ActiveOperations = Set.empty)
  extends RunnableState {

  def startOp: Operation.BpmnId = {
    processDef
      .opDefs
      .collectFirst({ case x: StartOperationDef => x.bpmnId }).get
  }

  def activate(to: List[Operation.BpmnId], from: Option[Operation.BpmnId]): ProcessState = {
    val x = activeOperations ++ to
    copy(activeOperations = from.fold(x)(x - _))
  }

  def update: Update = {
    case ActivatedEvt(x, y) => activate(x, y)
  }
}

object ProcessState {
  def empty = ProcessState(
    processDef = ProcessDef.empty,
    storage = RunnableStorage.empty,
    activeOperations = Set.empty)

  def apply(processDef: ProcessDef): ProcessState = ProcessState(
    processDef = processDef,
    storage = RunnableStorage(processDef.defStorage))
}