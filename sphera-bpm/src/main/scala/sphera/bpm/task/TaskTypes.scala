package sphera.bpm.task

import sphera.bpm.history.model.TaskHistoryData
import sphera.bpm.runnable.RunnableManagerType.TaskManagerType
import sphera.bpm.runnable._
import sphera.bpm.task.actor.{ TaskManagerState, TaskState }

import scala.reflect.classTag
import scala.reflect.runtime.universe._

/**
 * Concrete Task types to be mixed in
 */
trait TaskGeneralTypes extends RunnableGeneralTypes {
  type Info = TaskInfo
  type CreateInfo = CreateTaskInfo
  type UpdateInfo = UpdateTaskInfo
  type ExtendedInfo = TaskExtendedInfo

  val ctag1 = classTag[Info]
  val ttag1 = typeTag[Info]
  val ctag2 = classTag[CreateInfo]
  val ctag3 = classTag[UpdateInfo]
  val ctag4 = classTag[ExtendedInfo]
}

trait TaskManagerTypes extends TaskGeneralTypes with RunnableManagerTypes {
  type State = TaskManagerState
  type ConcreteRunnableState = TaskState
  type ConcreteRunnable = Task
  type ConcreteRunnableHistoryData = TaskHistoryData

  val ctag5 = classTag[ConcreteRunnableState]
  val ctag6 = classTag[ConcreteRunnable]
  val ctag7 = classTag[State]
  val ctag8 = classTag[ConcreteRunnableHistoryData]

  val managerType = TaskManagerType
}

trait TaskManagerStateTypes extends TaskGeneralTypes with RunnableManagerStateTypes

trait TaskTypes extends TaskGeneralTypes with RunnableTypes {
  type State <: TaskState
}
