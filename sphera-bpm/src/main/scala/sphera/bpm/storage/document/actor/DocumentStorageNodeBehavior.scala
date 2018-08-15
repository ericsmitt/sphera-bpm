package sphera.bpm.storage.document.actor

import sphera.bpm.storage.definition.actor.DefStorageNodeBehavior
import sphera.bpm.storage.document.DocumentStorage

trait DocumentStorageNodeBehavior extends DefStorageNodeBehavior[DocumentStorage] {
  type State <: DocumentStorageNodeStateLike

  def documentStorageBehavior(state: State): Receive = PartialFunction.empty
}