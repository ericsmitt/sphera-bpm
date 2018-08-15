package sphera.bpm.storage.json.actor

import sphera.bpm.model.Modifier
import sphera.bpm.storage.Storage
import sphera.bpm.storage.Storage.NodeUpdatedTry
import sphera.bpm.storage.json.JsonStorage._
import sphera.bpm.utils.Printable
import sphera.core.akkaext.actor.{ CqrsEvent, FutureSupport }
import sphera.core.akkaext.persistence._

import scala.concurrent.{ Future, Promise }
import scala.util.{ Failure, Success, Try }

trait JsonStorageNodeBehavior extends CqrsPersistentActorLike with Printable with FutureSupport {
  type State <: JsonStorageNodeStateLike

  def tryUpdate(state: State, event: Storage.Event, modifier: Modifier): Unit = {
    Try(state.updated(event)) match {
      case Success(updatedState) =>
        persist(event) { event =>
          changeState(updatedState)
          sender() ! NodeUpdatedTry(Success(id))
          log.info(s"node ${"updated".blue} [${id.name}]")
        }
      case Failure(e) =>
        sender() ! NodeUpdatedTry(Failure(e))
        if (modifier.isActor) self ! e
        e.printStackTrace()
        log.info(s"node ${"failed".red} update [${id.name}, event: $event]")
    }
  }

  def jsonStorageBehavior(state: State): Receive = {
    case GetJson(path) => sender() ! JsonOpt(state.jsonStorage(path))
    case UpdateJsonCmd(x, y, z) => tryUpdate(state, UpdatedJsonEvt(x, y, z), z)
    case AddJsonCmd(x, y, z) => tryUpdate(state, AddedJsonEvt(x, y, z), z)
    case DeleteJsonCmd(x, z) => tryUpdate(state, DeletedJsonEvt(x, z), z)
  }
}