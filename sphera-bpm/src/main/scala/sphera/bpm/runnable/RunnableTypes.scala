package sphera.bpm.runnable

import scala.reflect._
import scala.reflect.runtime.universe._

/**
 * All concrete types in one place
 *
 * Runnable type structure
 */
trait RunnableGeneralTypes {
  type Info <: RunnableInfo
  type CreateInfo <: CreateRunnableInfo
  type UpdateInfo <: UpdateRunnableInfo
  type ExtendedInfo <: RunnableExtendedInfo

  implicit val ctag1: ClassTag[Info]
  implicit val ttag1: TypeTag[Info]
  implicit val ctag2: ClassTag[CreateInfo]
  implicit val ctag3: ClassTag[UpdateInfo]
  implicit val ctag4: ClassTag[ExtendedInfo]
}

trait RunnableManagerTypes extends RunnableGeneralTypes {
  type State <: RunnableManagerState
  type ConcreteRunnableState <: RunnableState
  type ConcreteRunnable <: RunnableLike
  type ConcreteRunnableHistoryData <: RunnableHistoryData

  implicit val ctag5: ClassTag[ConcreteRunnableState]
  implicit val ctag6: ClassTag[ConcreteRunnable]
  implicit val ctag7: ClassTag[State]
  implicit val ctag8: ClassTag[ConcreteRunnableHistoryData]
}

trait RunnableManagerStateTypes extends RunnableGeneralTypes

trait RunnableTypes extends RunnableGeneralTypes {
  type State <: RunnableState

  implicit val ctag7: ClassTag[State]
}