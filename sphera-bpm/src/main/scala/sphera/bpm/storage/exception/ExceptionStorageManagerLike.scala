package sphera.bpm.storage.exception

import java.time.ZonedDateTime

import akka.actor.ActorRef
import sphera.bpm.BpmException
import sphera.bpm.storage.exception.ExceptionStorage.{ ExceptionsMap, GetExceptions }
import sphera.bpm.storage.{ Node, NodeContainer }

import scala.concurrent.Future

trait ExceptionStorageManagerLike extends NodeContainer {
  def getExceptions(nodeId: Node.Id): Future[Map[ZonedDateTime, BpmException]] = getNode(nodeId).flatMap(getExceptions)

  def getExceptions(node: ActorRef): Future[Map[ZonedDateTime, BpmException]] =
    ask(node, GetExceptions)
      .mapTo[ExceptionsMap]
      .map(_.exceptions)
}