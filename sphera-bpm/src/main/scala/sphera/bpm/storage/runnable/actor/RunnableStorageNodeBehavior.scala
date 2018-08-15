package sphera.bpm.storage.runnable.actor

import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.storage.definition.actor.DefStorageNodeBehavior
import sphera.bpm.storage.exception.actor.ExceptionStorageNodeBehavior

trait RunnableStorageNodeBehavior extends DefStorageNodeBehavior[DefStorage] with ExceptionStorageNodeBehavior {
  type State <: RunnableStorageNodeStateLike

  override def preBehavior(state: State): Receive = {
    jsonStorageBehavior(state)
      .orElse(defStorageBehavior(state))
      .orElse(exceptionStorageBehavior(state))
  }
}
