package sphera.bpm.storage.exception.actor

import sphera.bpm.BpmException
import sphera.bpm.storage.Storage.NodeUpdated
import sphera.bpm.storage.exception.ExceptionStorage._
import sphera.core.akkaext.persistence.CqrsPersistentActorLike

trait ExceptionStorageNodeBehavior extends CqrsPersistentActorLike {
  type State <: ExceptionStorageNodeStateLike

  def persistException(state: State, exception: BpmException): Unit = {
    persist(state, AddedExceptionEvt(exception)) { (state, event) =>
      sender() ! NodeUpdated(id)
    }
  }

  def exceptionStorageBehavior(state: State): Receive = {
    case GetExceptions => sender() ! ExceptionsMap(state.exceptionStorage.exceptions)
    case PersistException(x) => persistException(state, x)
  }
}