package sphera.bpm.storage.document.actor

import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.storage.definition.actor.DefStorageNodeStateLike
import sphera.bpm.storage.document.DocumentStorage

trait DocumentStorageNodeStateLike extends DefStorageNodeStateLike[DocumentStorage] {
  def defStorage = documentStorage
  def documentStorage: DocumentStorage

  def withDefStorage(defStorage: DocumentStorage): DefStorageNodeStateLike[DocumentStorage] = {
    withDocumentStorage(defStorage.asInstanceOf[DocumentStorage])
  }

  def withDocumentStorage(documentStorage: DocumentStorage): DocumentStorageNodeStateLike

  def documentStorageUpdate: Update = PartialFunction.empty
}
