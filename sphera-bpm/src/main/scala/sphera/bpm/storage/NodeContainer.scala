package sphera.bpm.storage

import akka.actor.ActorRef
import sphera.bpm.NodeNotFoundException
import sphera.bpm.storage.StorageManager.{ GetNode, NodeOpt }
import sphera.core.akkaext.actor.{ ActorId, FutureSupport }

import scala.concurrent.Future

trait NodeContainer extends FutureSupport {
  def nodeContainerActor: ActorRef

  def getNode(nodeId: ActorId): Future[ActorRef] =
    nodeContainerActor.ask(GetNode(nodeId))
      .mapTo[NodeOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw NodeNotFoundException(nodeId)
      }

  def getNodeOpt(nodeId: ActorId): Future[NodeOpt] =
    nodeContainerActor.ask(GetNode(nodeId))
      .mapTo[NodeOpt]
}
