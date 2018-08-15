package sphera.bpm.process

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.process.Process._
import sphera.bpm.process.actor.operation.TaskOperationActor
import sphera.bpm.runnable.RunnableLike
import sphera.bpm.task.TaskInfo
import sphera.core.akkaext.actor._

import scala.concurrent.{ ExecutionContext, Future }

class Process(val actorRef: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableLike with ProcessTypes {
  def operations: Future[Set[OperationDef]] = getState.map(_.processDef.opDefs)

  def activeOperations: Future[Set[OperationDef]] = getState.map { x =>
    val ops = x.activeOperations
    x.processDef.opDefs.filter(ops contains _.bpmnId)
  }

  def inclusiveGateways: Future[Set[InclusiveGatewayOperationDef]] = operations.map { v =>
    v collect {
      case x: InclusiveGatewayOperationDef => x
    }
  }

  def getInclusiveGatewaysByOutgoing(op: Operation.BpmnId): Future[Option[InclusiveGatewayOperationDef]] = {
    inclusiveGateways.map(_.find(_.conditions.exists(_.operation == op)))
  }

  def activate(to: Operation.BpmnId, from: Option[Operation.BpmnId]): Unit = {
    actorRef ! ActivateCmd(List(to), from)
  }

  def activate(to: List[Operation.BpmnId], from: Option[Operation.BpmnId]): Unit = {
    actorRef ! ActivateCmd(to, from)
  }

  def completeTaskOperation(taskInfo: TaskInfo): Future[TaskInfo] = {
    getRunnable(taskInfo.relaredOperationId.get)
      .flatMap(x => x.actorRef.ask(TaskOperationActor.CompleteTaskCmd(taskInfo)))
      .map(_ => taskInfo)
  }
}

object Process {
  type Id = ActorId
  type RawId = String
  type BpmnId = sphera.bpm.bpmn.BpmnId

  trait Command extends CqrsCommand
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case class ActivateCmd(to: List[Operation.BpmnId], from: Option[Operation.BpmnId]) extends Command

  case class ActivatedEvt(to: List[Operation.BpmnId], from: Option[Operation.BpmnId]) extends Event

  case object Done extends Response
  case object OperationNotFound extends Response
}