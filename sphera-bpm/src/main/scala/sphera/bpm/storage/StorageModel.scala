package sphera.bpm.storage

import sphera.bpm.storage.definition._
import sphera.bpm.storage.json.JsonStorageLike
import sphera.core.akkaext.actor.ActorId

object Node {
  type Id = ActorId
}

case class NodeInfo(
  id: Node.Id,
  name: Name,
  description: Description,
  status: NodeStatus)

case class CreateNodeInfo(
  name: Name,
  description: Description,
  storage: JsonStorageLike)

case class UpdateNodeInfo(
  id: Node.Id,
  name: Option[Name] = None,
  description: Option[Description] = None,
  status: Option[NodeStatus] = None)

sealed trait NodeStatus {
  def name: String
  override def toString = this.name
}

object NodeStatus {
  case object Created extends NodeStatus {
    val name = "created"
  }
  case object Started extends NodeStatus {
    val name = "started"
  }
  case object Deleted extends NodeStatus {
    val name = "deleted"
  }
}

