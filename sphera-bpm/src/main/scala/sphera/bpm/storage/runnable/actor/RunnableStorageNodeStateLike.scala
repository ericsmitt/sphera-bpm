package sphera.bpm.storage.runnable.actor

import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.storage.definition.actor.DefStorageNodeStateLike
import sphera.bpm.storage.exception.ExceptionStorage
import sphera.bpm.storage.exception.actor.ExceptionStorageNodeStateLike
import sphera.bpm.storage.runnable.RunnableStorage

trait RunnableStorageNodeStateLike extends DefStorageNodeStateLike[DefStorage] with ExceptionStorageNodeStateLike {
  def defStorage: DefStorage = storage.defStorage
  def exceptionStorage: ExceptionStorage = storage.exceptionStorage
  def storage: RunnableStorage

  def withRunnableStorage(runnableStorage: RunnableStorage): RunnableStorageNodeStateLike

  override def preUpdate: Update = {
    jsonStorageUpdate
      .orElse(defStorageUpdate)
      .orElse(exceptionStorageUpdate)
  }
}
