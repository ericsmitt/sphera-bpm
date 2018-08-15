package sphera.bpm.storage.definition.actor

import sphera.bpm.storage.definition.DefStorage

case class DefStorageNodeState(defStorage: DefStorage) extends DefStorageNodeStateLike[DefStorage] {
  def withDefStorage(defStorage: DefStorage): DefStorageNodeState = copy(defStorage = defStorage)
  def update: Update = defStorageUpdate.orElse(jsonStorageUpdate)
}

object DefStorageNodeState {
  def empty: DefStorageNodeState = DefStorageNodeState(DefStorage.emptyRootDef())
}