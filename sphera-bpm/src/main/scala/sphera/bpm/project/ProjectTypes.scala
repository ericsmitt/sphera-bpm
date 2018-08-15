package sphera.bpm.project

import sphera.bpm.history.model.ProjectHistoryData
import sphera.bpm.project.actor.{ ProjectManagerState, ProjectState }
import sphera.bpm.runnable.RunnableManagerType.ProjectManagerType
import sphera.bpm.runnable._

import scala.reflect.classTag
import scala.reflect.runtime.universe._

/**
 * Concrete ProjectTypes types to be mixed in
 */
trait ProjectGeneralTypes extends RunnableGeneralTypes {
  type Info = ProjectInfo
  type CreateInfo = CreateProjectInfo
  type UpdateInfo = UpdateProjectInfo
  type ExtendedInfo = ProjectExtendedInfo

  val ctag1 = classTag[Info]
  val ttag1 = typeTag[Info]
  val ctag2 = classTag[CreateInfo]
  val ctag3 = classTag[UpdateInfo]
  val ctag4 = classTag[ExtendedInfo]
}

trait ProjectManagerTypes extends ProjectGeneralTypes with RunnableManagerTypes {
  type State = ProjectManagerState
  type ConcreteRunnableState = ProjectState
  type ConcreteRunnable = Project
  type ConcreteRunnableHistoryData = ProjectHistoryData

  val ctag5 = classTag[ConcreteRunnableState]
  val ctag6 = classTag[ConcreteRunnable]
  val ctag7 = classTag[State]
  val ctag8 = classTag[ConcreteRunnableHistoryData]

  val managerType = ProjectManagerType
}

trait ProjectManagerStateTypes extends ProjectGeneralTypes with RunnableManagerStateTypes

trait ProjectTypes extends ProjectGeneralTypes with RunnableTypes {
  type State = ProjectState

  val ctag7 = classTag[State]
}
