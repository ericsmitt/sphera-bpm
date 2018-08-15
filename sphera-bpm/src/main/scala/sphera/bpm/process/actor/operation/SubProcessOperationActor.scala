package sphera.bpm.process.actor.operation

import java.time.ZonedDateTime

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.Assignments
import sphera.bpm.masterdata.model.{ DataStructure, ProcessTemplate }
import sphera.bpm.process.{ CreateProcessInfo, Operation, Process, ProcessInfo }
import sphera.bpm.project.Project
import sphera.bpm.runnable.{ RecoverInfo, RunnableType }
import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.task._
import sphera.core.domain.tenancy.model.User
import io.circe.Json

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }

class SubProcessOperationActor(
  val id: Operation.Id,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val data: Json,
  val templateId: ProcessTemplate.Id,
  val loopCharacteristics: LoopCharacteristics,
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
    def createSubProcess(info: ProcessInfo) = {
      bpm.createProcessAndGetId(
        createInfo = CreateProcessInfo(
          name = name,
          description = None,
          data = data,
          parentId = Some(processId),
          projectId = projectId,
          initiatorId = Some(id),
          managerId = info.managerId,
          relatedOperationId = Some(id),
          relatedOperationBpmnId = Some(bpmnId),
          templateId = templateId),
        recoverInfo = recoverInfo.map(_.copy(RunnableType.Process)))
    }

    def createTask(managerId: User.Id, subProcessId: Process.Id) = {
      bpm.createTask(
        createInfo = CreateTaskInfo(
          name = name,
          description = None,
          taskType = TaskType.SubProcessTask,
          initiator = Initiator.ProcessManager,
          ownerId = managerId,
          assigneeId = None,
          reviewerId = None,
          watcherIds = Set.empty,
          parentId = None,
          processId = Some(processId),
          subProcessId = Some(subProcessId),
          relatedOperationId = Some(id),
          relatedOperationBpmnId = Some(bpmnId),
          formTemplateId = None,
          defStorage = defStorage,
          plannedStart = plannedStart,
          plannedDuration = plannedDuration),
        recoverInfo = recoverInfo)
    }

    for {
      r1 <- bpm.getProcessInfo(processId)
      r2 <- createSubProcess(r1)
      r3 <- createTask(r1.managerId, r2)
    } yield r3
  }
}

object SubProcessOperationActor {
  def props(
    id: Operation.Id,
    bpmnId: Operation.BpmnId,
    name: Operation.Name,
    data: Json,
    templateId: ProcessTemplate.Id,
    loopCharacteristics: LoopCharacteristics,
    taskDataStructureId: Option[DataStructure.Id],
    plannedStart: Option[ZonedDateTime],
    plannedDuration: Option[FiniteDuration],
    projectId: Project.Id,
    processId: Process.Id,
    preAssignments: Assignments,
    postAssignments: Assignments,
    in: List[Operation.BpmnId],
    out: List[Operation.BpmnId],
    bpm: Bpm)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new SubProcessOperationActor(
      id = id,
      bpmnId = bpmnId,
      name = name,
      data = data,
      templateId = templateId,
      loopCharacteristics = loopCharacteristics,
      dataStructureId = taskDataStructureId,
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