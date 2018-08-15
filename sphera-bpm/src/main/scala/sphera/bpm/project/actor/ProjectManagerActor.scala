package sphera.bpm.project.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.project._
import sphera.bpm.runnable.{ RunnableManagerBehavior, RunnableManagerState }

import scala.concurrent.ExecutionContext

class ProjectManagerActor(
  val id: ProjectManager.Id,
  val initState: ProjectManagerState,
  val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableManagerBehavior
  with ProjectManagerTypes

object ProjectManagerActor {
  def props(
    id: ProjectManager.Id,
    bpm: Bpm,
    state: ProjectManagerState = ProjectManagerState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ProjectManagerActor(id = id, bpm = bpm, initState = state))
  }
}

case class ProjectManagerState(storage: Map[Project.RawId, ProjectInfo]) extends RunnableManagerState
  with ProjectManagerStateTypes

object ProjectManagerState {
  def empty = ProjectManagerState(Map.empty)
}

