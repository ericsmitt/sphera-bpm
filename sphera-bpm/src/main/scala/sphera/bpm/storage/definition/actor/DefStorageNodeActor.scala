package sphera.bpm.storage.definition.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.storage.definition.DefStorage
import sphera.core.akkaext.actor.ActorId
import sphera.core.akkaext.persistence._

import scala.concurrent.ExecutionContext

class DefStorageNodeActor(val id: ActorId, val bpm: Bpm, val initState: DefStorageNodeState)(implicit val c: ExecutionContext, val t: Timeout)
  extends CqrsPersistentActor[DefStorageNodeState] with DefStorageNodeBehavior[DefStorage] {

  def behavior(state: DefStorageNodeState): Receive =
    defStorageBehavior(state)
      .orElse(jsonStorageBehavior(state))
}

object DefStorageNodeActor {
  def props(id: ActorId, bpm: Bpm, state: DefStorageNodeState = DefStorageNodeState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new DefStorageNodeActor(id, bpm, state))
  }
}
