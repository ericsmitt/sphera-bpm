package sphera.bpm.task

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.core.akkaext.actor._

import scala.concurrent.ExecutionContext

class TaskManager(
  val taskManagerActor: ActorRef,
  val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends TaskManagerLike

object TaskManager {
  type Id = ActorId

  def apply(taskManagerActor: ActorRef, bpm: Bpm)(implicit c: ExecutionContext, t: Timeout): TaskManager = {
    new TaskManager(taskManagerActor, bpm)
  }
}
