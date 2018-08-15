package sphera.bpm.storage.document.actor

import sphera.bpm.storage.Node
import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.storage.definition.actor.DefStorageNodeState
import sphera.bpm.storage.document.DocumentStorage

case class DocumentStorageNodeState(documentStorage: DocumentStorage) extends DocumentStorageNodeStateLike {
  def withDocumentStorage(documentStorage: DocumentStorage): DocumentStorageNodeState = copy(documentStorage = documentStorage)
  def update: Update = documentStorageUpdate.orElse(jsonStorageUpdate)
}

object DocumentStorageNodeState {
  def empty(id: Node.Id): DocumentStorageNodeState = {
    DocumentStorageNodeState(DocumentStorage.emptyRootDef(
      id = id))
  }
}

