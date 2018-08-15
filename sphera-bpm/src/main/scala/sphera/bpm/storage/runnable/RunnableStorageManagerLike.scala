package sphera.bpm.storage.runnable

import sphera.bpm.storage.definition.DefStorageManagerLike
import sphera.bpm.storage.document.DocumentStorageManagerLike
import sphera.bpm.storage.exception.ExceptionStorageManagerLike

trait RunnableStorageManagerLike extends DefStorageManagerLike
  with DocumentStorageManagerLike
  with ExceptionStorageManagerLike