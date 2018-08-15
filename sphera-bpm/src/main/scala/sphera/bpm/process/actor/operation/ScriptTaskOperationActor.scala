package sphera.bpm.process.actor.operation

import java.time.ZonedDateTime

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.Assignments
import sphera.bpm.masterdata.model.DataStructure
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.bpm.runnable.RecoverInfo
import sphera.bpm.storage.definition.DefStorage
import sphera.bpm.task._
import sphera.core.akkaext.actor.ActorId
import sphera.core.domain.tenancy.model.User

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }

class ScriptTaskOperationActor(
  val id: Operation.Id,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val loopCharacteristics: LoopCharacteristics,
  val dataStructureId: Option[DataStructure.Id],
  val plannedStart: Option[ZonedDateTime],
  val plannedDuration: Option[FiniteDuration],
  val in: List[Operation.BpmnId],
  val out: List[Operation.BpmnId],
  val javascriptCode: Option[String],
  val additionalScopeMappings: Map[String, ActorId],
  val preAssignments: Assignments,
  val postAssignments: Assignments,
  val projectId: Project.Id,
  val processId: Process.Id,
  val bpm: Bpm)(implicit val c: ExecutionContext, val t: Timeout) extends TaskOperationActor {

  def createTask(defStorage: DefStorage, recoverInfo: Option[RecoverInfo]): Future[TaskInfo] = {
    def create(managerId: User.Id) = {
      val createInfo = CreateTaskInfo(
        name = name,
        description = None,
        taskType = TaskType.ScriptTask,
        initiator = Initiator.ProcessManager,
        ownerId = managerId,
        assigneeId = None,
        reviewerId = None,
        watcherIds = Set.empty,
        parentId = None,
        processId = Option(processId),
        subProcessId = None,
        relatedOperationId = Option(id),
        relatedOperationBpmnId = Option(bpmnId),
        formTemplateId = None,
        defStorage = defStorage,
        plannedStart = plannedStart,
        plannedDuration = plannedDuration)

      bpm.taskManager.createTask(createInfo, recoverInfo)
    }

    bpm.getProcessInfo(processId)
      .map(_.managerId)
      .flatMap(create)
  }
}

object ScriptTaskOperationActor {
  def props(
    id: Operation.Id,
    bpmnId: Operation.BpmnId,
    name: Operation.Name,
    loopCharacteristics: LoopCharacteristics,
    dataStructureId: Option[DataStructure.Id],
    plannedStart: Option[ZonedDateTime],
    plannedDuration: Option[FiniteDuration],
    in: List[Operation.BpmnId],
    out: List[Operation.BpmnId],
    javascriptCode: Option[String],
    additionalScopeMappings: Map[String, ActorId],
    preAssignments: Assignments,
    postAssignments: Assignments,
    projectId: Project.Id,
    processId: Process.Id,
    bpm: Bpm)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ScriptTaskOperationActor(
      id = id,
      bpmnId = bpmnId,
      name = name,
      loopCharacteristics = loopCharacteristics,
      dataStructureId = dataStructureId,
      plannedStart = plannedStart,
      plannedDuration = plannedDuration,
      in = in,
      out = out,
      javascriptCode = javascriptCode,
      additionalScopeMappings = additionalScopeMappings,
      preAssignments = preAssignments,
      postAssignments = postAssignments,
      projectId = projectId,
      processId = processId,
      bpm = bpm))
  }
}
