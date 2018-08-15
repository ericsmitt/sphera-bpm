package sphera.bpm.project

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.project.Project.ActiveProjects
import sphera.bpm.runnable.RunnableLike
import sphera.core.akkaext.actor.{ ActorId, CqrsCommand, CqrsEvent, CqrsResponse }

import scala.concurrent.ExecutionContext

class Project(val actorRef: ActorRef)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableLike with ProjectTypes

object ActiveProjects {
  def empty: ActiveProjects = Set.empty
}

object Project {
  type Id = ActorId
  type RawId = String
  type ActiveProjects = Set[Project.Id]

  trait Command extends CqrsCommand
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case object Done extends Response
}
