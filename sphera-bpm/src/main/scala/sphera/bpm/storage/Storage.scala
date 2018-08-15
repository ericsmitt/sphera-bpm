package sphera.bpm.storage

import sphera.core.akkaext.actor._
import sphera.bpm.json._
import scala.util.Try

object Storage {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent {
    override def toString = this.asJsonStr
  }

  case class NodeUpdatedTry(nodeIdTry: Try[Node.Id]) extends Response
  case class NodeUpdated(nodeId: Node.Id) extends Response
}
