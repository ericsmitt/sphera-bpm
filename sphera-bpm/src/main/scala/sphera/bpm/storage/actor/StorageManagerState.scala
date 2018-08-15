package sphera.bpm.storage.actor

import sphera.bpm.storage.StorageManager._
import sphera.bpm.storage._
import sphera.core.akkaext.actor._
import sphera.bpm.storage.Node._

case class StorageManagerState(state: Map[RawPath, NodeInfo]) extends CqrsState {
  def createNodeInfo(x: NodeInfo): StorageManagerState = copy(state + (x.id.raw -> x))

  def updateNodeInfo(updateInfo: UpdateNodeInfo): StorageManagerState = {
    val x = state(updateInfo.id.raw)
    val info = NodeInfo(
      id = x.id,
      name = updateInfo.name.getOrElse(x.name),
      description = updateInfo.description.getOrElse(x.description),
      status = updateInfo.status.getOrElse(x.status))
    copy(state + (updateInfo.id.raw -> info))
  }

  def deleteNodeInfo(x: Node.Id): StorageManagerState = copy(state - x.raw)
  def nodeInfoExists(x: Node.Id): Boolean = state.get(x.raw).isDefined
  def getNodeInfo(x: Node.Id): NodeInfo = state(x.raw)
  def getNodeInfoOpt(storageId: Node.Id): Option[NodeInfo] = state.get(storageId.raw)
  def getNodeInfoMap: Map[RawPath, NodeInfo] = state
  def getNodeIds: Set[Node.Id] = state.keys.map(ActorId.apply).toSet

  def update = {
    case CreatedNodeInfoEvt(x) => createNodeInfo(x)
    case UpdatedNodeInfoEvt(x) => updateNodeInfo(x)
    case DeletedNodeInfoEvt(x) => deleteNodeInfo(x)
  }
}

object StorageManagerState {
  def empty = StorageManagerState(Map.empty)
}
