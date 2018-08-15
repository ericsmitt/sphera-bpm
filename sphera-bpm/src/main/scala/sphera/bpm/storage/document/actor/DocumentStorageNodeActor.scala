package sphera.bpm.storage.document.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.storage.definition.actor.{ DefStorageNodeActor, DefStorageNodeState }
import sphera.bpm.storage.document.DocumentStorage
import sphera.core.akkaext.actor.ActorId
import sphera.core.akkaext.persistence._

import scala.concurrent.ExecutionContext

class DocumentStorageNodeActor(val id: ActorId, val bpm: Bpm, val initState: DocumentStorageNodeState)(implicit val c: ExecutionContext, val t: Timeout)
  extends CqrsPersistentActor[DocumentStorageNodeState] with DocumentStorageNodeBehavior {

  def behavior(state: DocumentStorageNodeState): Receive =
    documentStorageBehavior(state)
      .orElse(defStorageBehavior(state))
      .orElse(jsonStorageBehavior(state))
}

object DocumentStorageNodeActor {
  def props(id: ActorId, bpm: Bpm, state: DocumentStorageNodeState)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new DocumentStorageNodeActor(
      id = id,
      bpm = bpm,
      initState = state))
  }
}

