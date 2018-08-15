package sphera.bpm.task

import java.time._

import sphera.bpm.masterdata.model.FormTemplate
import sphera.bpm.model.{ Modifier, ModifyAttr }
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.runnable._
import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.task.actor.TaskState
import sphera.core.domain.tenancy.model.User

import scala.concurrent.duration.FiniteDuration

case class TaskInfo(
  id: Task.Id,
  name: String,
  description: Option[String],
  taskType: TaskType,
  status: RunnableStatus,
  initiator: Initiator,
  ownerId: User.Id,
  assigneeId: Option[User.Id],
  reviewerId: Option[User.Id],
  watcherIds: Set[User.Id],
  parentId: Option[Task.Id],
  childIds: Set[Task.Id],
  activeChildIds: Set[Task.Id],
  processId: Option[Process.Id],
  subProcessId: Option[Process.Id],
  relaredOperationId: Option[Operation.Id],
  relatedOperationBpmnId: Option[Operation.BpmnId],
  formTemplateId: Option[FormTemplate.Id],
  startedBy: Option[Modifier],
  startedOn: Option[ZonedDateTime],
  actualEnd: Option[ZonedDateTime],
  plannedStart: Option[ZonedDateTime],
  plannedDuration: Option[FiniteDuration],
  modifyAttr: ModifyAttr) extends RunnableInfo {

  def withStatus(status: RunnableStatus) = copy(status = status)
}

case class CreateTaskInfo(
  name: String,
  description: Option[String],
  taskType: TaskType,
  initiator: Initiator,
  ownerId: User.Id,
  assigneeId: Option[User.Id],
  reviewerId: Option[User.Id],
  watcherIds: Set[User.Id],
  parentId: Option[Task.Id],
  processId: Option[Process.Id],
  subProcessId: Option[Process.Id],
  relatedOperationId: Option[Operation.Id],
  relatedOperationBpmnId: Option[Operation.BpmnId],
  formTemplateId: Option[FormTemplate.Id],
  defStorage: DefStorage,
  plannedStart: Option[ZonedDateTime],
  plannedDuration: Option[FiniteDuration]) extends CreateRunnableInfo

case class UpdateTaskInfo(
  id: Task.Id,
  name: Option[String] = None,
  description: Option[Option[String]] = None,
  status: Option[RunnableStatus] = None,
  initiator: Option[Initiator] = None,
  assigneeId: Option[Option[User.Id]] = None,
  reviewerId: Option[Option[User.Id]] = None,
  watcherIds: Option[Set[User.Id]] = None,
  childIds: Option[Set[Task.Id]] = None,
  activeChildIds: Option[Set[Task.Id]] = None,
  startedBy: Option[Option[Modifier]] = None,
  startedOn: Option[Option[ZonedDateTime]] = None,
  actualEnd: Option[Option[ZonedDateTime]] = None) extends UpdateRunnableInfo

case class TaskExtendedInfo(info: TaskInfo, state: TaskState) extends RunnableExtendedInfo

sealed trait TaskType

object TaskType {
  case object UserTask extends TaskType
  case object SendTask extends TaskType
  case object ScriptTask extends TaskType
  case object ReceiveTask extends TaskType
  case object StartProcessTask extends TaskType
  case object StartProjectProcessTask extends TaskType
  case object StartProjectTask extends TaskType
  case object SubProcessTask extends TaskType
}

sealed trait Initiator

object Initiator {
  case object ProjectManager extends Initiator
  case object ProcessManager extends Initiator
}