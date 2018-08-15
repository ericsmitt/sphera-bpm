package sphera.bpm.process

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.core.akkaext.actor._

import scala.concurrent.ExecutionContext

class ProcessManager(
  val processManagerActor: ActorRef,
  val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout)
  extends ProcessManagerLike

object ProcessManager {
  type Id = ActorId

  def apply(processManagerActor: ActorRef, bpm: Bpm)(implicit c: ExecutionContext, t: Timeout): ProcessManager = {
    new ProcessManager(processManagerActor, bpm)
  }
}