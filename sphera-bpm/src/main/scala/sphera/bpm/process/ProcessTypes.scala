package sphera.bpm.process

import sphera.bpm.history.model.ProcessHistoryData
import sphera.bpm.process.actor.{ ProcessManagerState, ProcessState }
import sphera.bpm.runnable.RunnableManagerType.ProcessManagerType
import sphera.bpm.runnable._

import scala.reflect.classTag
import scala.reflect.runtime.universe._

/**
 * Concrete Process types to be mixed in
 */
trait ProcessGeneralTypes extends RunnableGeneralTypes {
  type Info = ProcessInfo
  type CreateInfo = CreateProcessInfo
  type UpdateInfo = UpdateProcessInfo
  type ExtendedInfo = ProcessExtendedInfo

  val ctag1 = classTag[Info]
  val ttag1 = typeTag[Info]
  val ctag2 = classTag[CreateInfo]
  val ctag3 = classTag[UpdateInfo]
  val ctag4 = classTag[ExtendedInfo]
}

trait ProcessManagerTypes extends ProcessGeneralTypes with RunnableManagerTypes {
  type State = ProcessManagerState
  type ConcreteRunnableState = ProcessState
  type ConcreteRunnable = Process
  type ConcreteRunnableHistoryData = ProcessHistoryData

  val ctag5 = classTag[ConcreteRunnableState]
  val ctag6 = classTag[ConcreteRunnable]
  val ctag7 = classTag[State]
  val ctag8 = classTag[ConcreteRunnableHistoryData]

  val managerType = ProcessManagerType
}

trait ProcessManagerStateTypes extends ProcessGeneralTypes with RunnableManagerStateTypes

trait ProcessTypes extends ProcessGeneralTypes with RunnableTypes {
  type State = ProcessState

  val ctag7 = classTag[State]
}
