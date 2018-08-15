package sphera.bpm.storage

import akka.actor.ActorRef
import sphera.bpm.NodeNotFoundException
import sphera.bpm.storage.StorageManager._
import sphera.bpm.storage.runnable.RunnableStorageManagerLike
import sphera.core.akkaext.actor.{ ActorId, RawPath }

import scala.concurrent.Future

trait StorageManagerLike extends RunnableStorageManagerLike {
  def storageManagerActor: ActorRef
  def nodeContainerActor: ActorRef = storageManagerActor

  def createNode(x: CreateNodeInfo): Future[NodeInfo] =
    ask(storageManagerActor, CreateNodeCmd(x))
      .mapTo[NodeInfoOpt]
      .map(_.x.get)

  def getNodeInfo(nodeId: ActorId): Future[NodeInfo] =
    ask(storageManagerActor, GetNodeInfoById(nodeId))
      .mapTo[NodeInfoOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw NodeNotFoundException(nodeId)
      }

  def getNodeInfoAll: Future[Map[RawPath, NodeInfo]] =
    ask(storageManagerActor, GetNodeInfoAll)
      .mapTo[NodeInfoMap]
      .map(_.x)

  def getNodeStatus(nodeId: ActorId): Future[NodeStatus] =
    ask(storageManagerActor, GetNodeStatus(nodeId))
      .mapTo[NodeStatusOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw NodeNotFoundException(nodeId)
      }
}
