package sphera.bpm.process

import akka.actor.ActorRef
import sphera.core.akkaext.actor._

case class Operation(actorRef: ActorRef)

object Operation {
  type Id = ActorId
  type BpmnId = sphera.bpm.bpmn.BpmnId
  type Name = String
  type Description = String
  type ActiveOperations = Set[Operation.BpmnId]

  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class ActivateCmd(from: Option[Operation.BpmnId]) extends Command

  case object Done extends Response
}