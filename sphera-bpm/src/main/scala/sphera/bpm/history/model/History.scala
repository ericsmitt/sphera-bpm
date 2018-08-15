package sphera.bpm.history.model

import java.time.ZonedDateTime

import sphera.bpm.process.{ Process, ProcessExtendedInfo }
import sphera.bpm.project._
import sphera.bpm.runnable.{ RunnableExtendedInfo, RunnableHistoryData }
import sphera.bpm.task.{ Task, TaskExtendedInfo }
import sphera.core.akkaext.actor.ActorId

trait HistoryData extends RunnableHistoryData {
  def id: HistoryData.Id
  def extendedInfo: RunnableExtendedInfo
}

object HistoryData {
  type Id = ActorId
  type RawId = String

  def apply(historyDataId: HistoryData.Id, extendedInfo: RunnableExtendedInfo, completedOn: Option[ZonedDateTime]): HistoryData = {
    extendedInfo match {
      case x: ProjectExtendedInfo => ProjectHistoryData(historyDataId, x, completedOn)
      case x: ProcessExtendedInfo => ProcessHistoryData(historyDataId, x, completedOn)
      case x: TaskExtendedInfo => TaskHistoryData(historyDataId, x, completedOn)
    }
  }
}

case class ProjectHistoryData(
  id: Project.Id,
  extendedInfo: ProjectExtendedInfo,
  completedOn: Option[ZonedDateTime]) extends HistoryData

case class ProcessHistoryData(
  id: Process.Id,
  extendedInfo: ProcessExtendedInfo,
  completedOn: Option[ZonedDateTime]) extends HistoryData

case class TaskHistoryData(
  id: Task.Id,
  extendedInfo: TaskExtendedInfo,
  completedOn: Option[ZonedDateTime]) extends HistoryData

