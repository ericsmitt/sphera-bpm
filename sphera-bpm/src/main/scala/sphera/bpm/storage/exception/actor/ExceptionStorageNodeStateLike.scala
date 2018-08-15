package sphera.bpm.storage.exception.actor

import sphera.bpm.storage.exception.ExceptionStorage
import sphera.bpm.storage.exception.ExceptionStorage.AddedExceptionEvt
import sphera.core.akkaext.actor.CqrsState

trait ExceptionStorageNodeStateLike extends CqrsState {
  def exceptionStorage: ExceptionStorage

  def withExceptionStorage(exceptionStorage: ExceptionStorage): ExceptionStorageNodeStateLike

  def exceptionStorageUpdate: Update = {
    case AddedExceptionEvt(x) => withExceptionStorage(exceptionStorage.persistException(x))
  }
}
