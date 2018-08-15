package sphera.bpm.storage.runnable

import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.storage.exception.ExceptionStorage

case class RunnableStorage(defStorage: DefStorage, exceptionStorage: ExceptionStorage)

object RunnableStorage {
  def empty = RunnableStorage(defStorage = DefStorage.emptyRootDef(), exceptionStorage = ExceptionStorage.empty)
  def apply(defStorage: DefStorage): RunnableStorage = new RunnableStorage(defStorage, ExceptionStorage.empty)
}