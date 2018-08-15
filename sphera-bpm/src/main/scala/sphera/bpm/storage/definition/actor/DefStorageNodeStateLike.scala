package sphera.bpm.storage.definition.actor

import sphera.bpm.storage.definition.{ DefStorage, DefStorageLike }
import sphera.bpm.storage.definition.DefStorage._
import sphera.bpm.storage.json.JsonStorageLike
import sphera.bpm.storage.json.actor.JsonStorageNodeStateLike

trait DefStorageNodeStateLike[A <: DefStorageLike[A]] extends JsonStorageNodeStateLike {
  def jsonStorage = defStorage
  def defStorage: A

  def withDefStorage(defStorage: A): DefStorageNodeStateLike[A]
  def withJsonStorage(jsonStorage: JsonStorageLike): JsonStorageNodeStateLike = withDefStorage(jsonStorage.asInstanceOf[A])

  def defStorageUpdate: Update = {
    case UpdatedDefValueEvt(x, y, z) => withDefStorage(defStorage.updateDefValue(x, y))
    // Update only def values
    case UpdatedDefValueOnlyEvt(x, y, z) => withDefStorage(defStorage.updateDefValueOnly(x, y))
    // Patch events
    case CreatedDefEvt(x, y, z) => withDefStorage(defStorage.createDef(x, y))
    case UpdatedDefEvt(x, y, z) => withDefStorage(defStorage.updateDef(x, y))
    case DeletedDefEvt(x, y, z) => withDefStorage(defStorage.deleteDef(x, y))
  }
}
