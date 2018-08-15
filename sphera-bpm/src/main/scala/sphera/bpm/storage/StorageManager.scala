package sphera.bpm.storage

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.core.akkaext.actor._

import scala.concurrent.ExecutionContext

class StorageManager(val storageManagerActor: ActorRef, val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends StorageManagerLike

object StorageManager {
  type Id = ActorId

  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateNodeCmd(x: CreateNodeInfo) extends Command
  case class UpdateNodeCmd(x: UpdateNodeInfo) extends Command
  case class DeleteNodeCmd(nodeId: ActorId) extends Command

  case class GetNodeInfoById(nodeId: ActorId) extends CqrsRequest
  case object GetNodeInfoAll extends CqrsRequest
  case class GetNodeStatus(nodeId: ActorId) extends CqrsRequest
  case class GetNode(nodeId: ActorId) extends CqrsRequest

  case class CreatedNodeInfoEvt(x: NodeInfo) extends Event
  case class UpdatedNodeInfoEvt(x: UpdateNodeInfo) extends Event
  case class DeletedNodeInfoEvt(nodeId: ActorId) extends Event

  case class NodeInfoOpt(x: Option[NodeInfo]) extends CqrsResponse
  case class NodeInfoMap(x: Map[String, NodeInfo]) extends CqrsResponse
  case class NodeStatusOpt(x: Option[NodeStatus]) extends CqrsResponse
  case class NodeOpt(x: Option[ActorRef]) extends CqrsResponse

  def apply(storageActor: ActorRef, bpm: Bpm)(implicit c: ExecutionContext, t: Timeout): StorageManager = {
    new StorageManager(storageManagerActor = storageActor, bpm = bpm)
  }
}