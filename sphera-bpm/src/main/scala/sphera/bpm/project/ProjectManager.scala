package sphera.bpm.project

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.core.akkaext.actor.ActorId

import scala.concurrent.ExecutionContext

class ProjectManager(val projectManagerActor: ActorRef, val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout)
  extends ProjectManagerLike

object ProjectManager {
  type Id = ActorId

  def apply(projectManagerActor: ActorRef, bpm: Bpm)(implicit c: ExecutionContext, t: Timeout): ProjectManager = {
    new ProjectManager(projectManagerActor, bpm)
  }
}