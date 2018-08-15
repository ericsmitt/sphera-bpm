package sphera.bpm.project.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.project.Project.ActiveProjects
import sphera.bpm.project._
import sphera.bpm.runnable.{ RunnableBehavior, RunnableState }
import sphera.bpm.storage.runnable.RunnableStorage

import scala.concurrent.ExecutionContext

class ProjectActor(
  val id: Project.Id,
  val bpm: Bpm,
  val initState: ProjectState)(implicit val c: ExecutionContext, val t: Timeout) extends RunnableBehavior with ProjectTypes {

  def historyManager = bpm.projectHistoryManager

  def start(state: ProjectState): Unit = {}

  def recover(state: ProjectState): Unit = {}

  def behavior(state: ProjectState): Receive = PartialFunction.empty
}

object ProjectActor {
  def props(projectId: Project.Id, bpm: Bpm, state: ProjectState = ProjectState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ProjectActor(
      id = projectId,
      bpm = bpm,
      initState = state))
  }
}

case class ProjectState(projectDef: ProjectDef, storage: RunnableStorage, activeProjects: ActiveProjects = ActiveProjects.empty)
  extends RunnableState {

  def update: Update = PartialFunction.empty
}

object ProjectState {
  def empty = ProjectState(
    projectDef = ProjectDef.empty,
    storage = RunnableStorage.empty,
    activeProjects = ActiveProjects.empty)

  def apply(projectDef: ProjectDef): ProjectState = ProjectState(
    projectDef = projectDef,
    storage = RunnableStorage(projectDef.defStorage))
}
