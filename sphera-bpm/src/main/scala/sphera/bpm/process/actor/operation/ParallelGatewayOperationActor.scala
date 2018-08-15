package sphera.bpm.process.actor.operation

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.Assignments
import sphera.bpm.process.Operation._
import sphera.bpm.process.actor.operation.ParallelGatewayOperationActor._
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.core.akkaext.actor._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * = ParallelGateway (Fork, Join) =
 *
 * Parallel Gateway is used to synchronize multiple concurrent branches (merging behavior). On the
 * other hand, it is used to spawn new concurrent threads on parallel branches (branching behavior).
 *
 * = Operational Semantics =
 *
 * - The Parallel Gateway is activated if there is at least one token on each incoming Sequence Flow.
 * - The Parallel Gateway consumes exactly one token from each incoming Sequence Flow and produces exactly one token at
 * each outgoing Sequence Flow.
 */
class ParallelGatewayOperationActor(
  val id: Operation.Id,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val in: List[Operation.BpmnId],
  val out: List[Operation.BpmnId],
  val assignments: Assignments,
  val projectId: Project.Id,
  val processId: Process.Id,
  val bpm: Bpm,
  val initState: ParallelGatewayOperationState)(implicit val c: ExecutionContext, val t: Timeout) extends PersistentOperationActor[ParallelGatewayOperationState] {

  def activate(state: State, from: Option[Operation.BpmnId]): Unit = {
    evalAssignments(assignments, createContext()).map { _ =>
      from match {
        case Some(op) if in.size > 1 => checkJoin(op).map {
          case Joined => deactivate()
          case NotJoined =>
        }
        case x if in.size == 1 => deactivate()
        case x =>
      }
    }
  }

  def checkJoin(op: Operation.BpmnId): Future[Response] = self.ask(JoinCmd(op)).mapTo[Response]

  /**
   * Joins multiple forked threads.
   * If there are excess tokens at an incoming Sequence Flow, these tokens remain at this Sequence Flow after execution
   * of the Gateway.
   */
  def join(state: ParallelGatewayOperationState, from: Operation.BpmnId): Unit = {
    val (joined, updatedState) = {
      if (state.join.contains(from)) false -> state.copy(excess = state.excess :+ from)
      else {
        val x1 = state.join :+ from
        if (x1.size == in.size) {
          val x2 = state.excess.distinct
          val x3 = state.excess diff state.excess.distinct
          true -> state.copy(join = x2, excess = x3)
        } else false -> state.copy(join = x1, excess = state.excess)
      }
    }
    persist(state, UpdatedStateEvt(updatedState)) { (state, event) =>
      if (joined) sender() ! Joined else sender() ! NotJoined
    }
  }

  //override def activateAfterRecover(state: State) = Future.unit

  override def postBehavior(state: ParallelGatewayOperationState): Receive = {
    case JoinCmd(from) => join(state, from)
  }
}

case class ParallelGatewayOperationState(join: List[Operation.BpmnId], excess: List[Operation.BpmnId]) extends CqrsState {
  def update: Update = {
    case UpdatedStateEvt(state) => state
  }
}

object ParallelGatewayOperationState {
  def empty = ParallelGatewayOperationState(List.empty, List.empty)
}

object ParallelGatewayOperationActor {
  trait Command extends CqrsCommand
  trait Event extends CqrsEvent

  case class JoinCmd(op: Operation.BpmnId) extends Command
  case class UpdatedStateEvt(state: ParallelGatewayOperationState) extends Event

  case object Joined extends Response
  case object NotJoined extends Response

  def props(
    id: Operation.Id,
    bpmnId: Operation.BpmnId,
    name: Operation.Name,
    in: List[Operation.BpmnId],
    out: List[Operation.BpmnId],
    assignments: Assignments,
    projectId: Project.Id,
    processId: Process.Id,
    bpm: Bpm,
    state: ParallelGatewayOperationState = ParallelGatewayOperationState.empty)(implicit c: ExecutionContext, t: Timeout): Props = {
    Props(new ParallelGatewayOperationActor(
      id = id,
      bpmnId = bpmnId,
      name = name,
      in = in,
      out = out,
      assignments = assignments,
      projectId = projectId,
      processId = processId,
      bpm = bpm,
      initState = state))
  }
}