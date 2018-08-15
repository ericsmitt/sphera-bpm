package sphera.bpm.process.actor.operation

import java.time.ZonedDateTime

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.Assignments
import sphera.bpm.masterdata.model.{ DataStructure, FormTemplate }
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.bpm.runnable.RecoverInfo
import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.task._
import sphera.core.akkaext.actor._
import sphera.core.domain.tenancy.model.User

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }

class UserTaskOperationActor(
  val id: Operation.Id,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val role: String,
  val assigneeId: Option[User.Id],
  val reviewerId: Option[User.Id],
  val watcherIds: Set[User.Id],
  val loopCharacteristics: LoopCharacteristics,
  val formTemplateId: FormTemplate.Id,
  val dataStructureId: Option[DataStructure.Id],
  val plannedStart: Option[ZonedDateTime],
  val plannedDuration: Option[FiniteDuration],
  val projectId: Project.Id,
  val processId: Process.Id,
  val preAssignments: Assignments,
  val postAssignments: Assignments,
  val in: List[Operation.BpmnId],
  val out: List[Operation.BpmnId],
  val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends TaskOperationActor {

  def createTask(defStorage: DefStorage, recoverInfo: Option[RecoverInfo]): Future[TaskInfo] = {
    bpm.findProcessRoleUser(processId, role) flatMap { ownerId =>
      val createInfo = CreateTaskInfo(
        name = name,
        description = None,
        taskType = TaskType.UserTask,
        initiator = Initiator.ProcessManager,
        ownerId = ownerId,
        assigneeId = None,
        reviewerId = reviewerId,
        watcherIds = watcherIds,
        parentId = None,
        processId = Option(processId),
        subProcessId = None,
        relatedOperationId = Option(id),
        relatedOperationBpmnId = Option(bpmnId),
        formTemplateId = Option(formTemplateId),
        defStorage = defStorage,
        plannedStart = plannedStart,
        plannedDuration = plannedDuration)

      bpm.taskManager.createTask(createInfo, recoverInfo)
    }
  }
}

object UserTaskOperationActor {
  def props(
    id: Operation.Id,
    bpmnId: Operation.BpmnId,
    name: Operation.Name,
    rolePath: RawPath,
    assigneeId: Option[User.Id],
    reviewerId: Option[User.Id],
    watcherIds: Set[User.Id],
    loopCharacteristics: LoopCharacteristics,
    formTemplateId: FormTemplate.Id,
    dataStructureId: DataStructure.Id,
    plannedStart: Option[ZonedDateTime],
    plannedDuration: Option[FiniteDuration],
    projectId: Project.Id,
    processId: Process.Id,
    preAssignments: Assignments,
    postAssignments: Assignments,
    in: List[Operation.BpmnId],
    out: List[Operation.BpmnId],
    bpm: Bpm)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new UserTaskOperationActor(
      id = id,
      bpmnId = bpmnId,
      name = name,
      role = rolePath,
      assigneeId = assigneeId,
      reviewerId = reviewerId,
      watcherIds = watcherIds,
      loopCharacteristics = loopCharacteristics,
      formTemplateId = formTemplateId,
      dataStructureId = Some(dataStructureId),
      plannedStart = plannedStart,
      plannedDuration = plannedDuration,
      projectId = projectId,
      processId = processId,
      preAssignments = preAssignments,
      postAssignments = postAssignments,
      in = in,
      out = out,
      bpm = bpm))
  }
}