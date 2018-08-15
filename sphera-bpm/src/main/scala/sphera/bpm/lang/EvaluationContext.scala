package sphera.bpm.lang

import sphera.bpm.storage.StorageManager
import sphera.core.akkaext.actor._

case class EvaluationContext(storage: StorageManager, scopeMappings: Map[RawPath, ActorId])