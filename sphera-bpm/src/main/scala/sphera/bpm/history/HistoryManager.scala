package sphera.bpm.history

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.core.akkaext.actor.ActorId

import scala.concurrent.ExecutionContext

class HistoryManager(val historyManagerActor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout)
  extends HistoryManagerLike

object HistoryManager {
  type Id = ActorId

  def apply(historyManagerActor: ActorRef)(implicit c: ExecutionContext, t: Timeout) =
    new HistoryManager(historyManagerActor)
}
