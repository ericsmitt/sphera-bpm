package sphera.bpm.storage.json.actor

import sphera.bpm.storage.json.JsonStorage._
import sphera.bpm.storage.json.JsonStorageLike
import sphera.core.akkaext.actor.CqrsState

trait JsonStorageNodeStateLike extends CqrsState {
  def jsonStorage: JsonStorageLike

  def withJsonStorage(jsonStorage: JsonStorageLike): JsonStorageNodeStateLike

  def jsonStorageUpdate: Update = {
    case UpdatedJsonEvt(x, y, z) => withJsonStorage(jsonStorage.updateJson(x, y))
    case AddedJsonEvt(x, y, z) => withJsonStorage(jsonStorage.addJson(x, y))
    case DeletedJsonEvt(x, z) => withJsonStorage(jsonStorage.deleteJson(x))
  }
}
