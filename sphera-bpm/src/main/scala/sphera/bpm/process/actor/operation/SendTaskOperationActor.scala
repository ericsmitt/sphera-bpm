package sphera.bpm.process.actor.operation

import java.time.ZonedDateTime

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.lang.{ Assignments, Expression, Expressions }
import sphera.bpm.masterdata.model.DataStructure
import sphera.bpm.process.Operation.BpmnId
import sphera.bpm.process.{ Operation, Process }
import sphera.bpm.project.Project
import sphera.bpm.runnable.RecoverInfo
import sphera.bpm.storage.definition.{ ArrayDef, DefStorage, StringDef }
import sphera.bpm.task._
import sphera.core.domain.tenancy.model.User

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }

class SendTaskOperationActor(
  val id: Operation.Id,
  val bpm: Bpm,
  val bpmnId: Operation.BpmnId,
  val name: Operation.Name,
  val loopCharacteristics: LoopCharacteristics,
  val dataStructureId: Option[DataStructure.Id],
  val plannedStart: Option[ZonedDateTime],
  val plannedDuration: Option[FiniteDuration],
  val in: List[Operation.BpmnId],
  val out: List[Operation.BpmnId],
  val recipients: Expressions,
  val subject: Expression,
  val message: Expression,
  val preAssignments: Assignments,
  val postAssignments: Assignments,
  val projectId: Project.Id,
  val processId: Process.Id)(implicit val c: ExecutionContext, val t: Timeout) extends TaskOperationActor {

  def createTask(defStorage: DefStorage, recoverInfo: Option[RecoverInfo]): Future[TaskInfo] = {
    val ctx = createContext()
    def create(managerId: User.Id, recipients: List[String], subject: String, message: String) = {
      val recipientsDef = recipients.foldLeft((0, List[StringDef]())) {
        case ((i, list), recipient) => (i + 1) -> (list :+ StringDef(
          name = s"recipient$i",
          description = None,
          index = None,
          value = recipient,
          path = s"recipient$i"))
      }._2

      val taskDef = Set(
        StringDef(
          name = "subject",
          description = None,
          index = None,
          value = subject,
          path = "subject"),
        StringDef(
          name = "message",
          description = None,
          index = None,
          value = message,
          path = "message"),
        ArrayDef(
          name = "recipients",
          description = None,
          value = recipientsDef,
          path = "recipients"))

      val createInfo = CreateTaskInfo(
        name = "SendTask",
        description = None,
        taskType = TaskType.SendTask,
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
        defStorage = defStorage.addDef(taskDef),
        plannedStart = plannedStart,
        plannedDuration = plannedDuration)

      bpm.taskManager.createTask(createInfo, recoverInfo)
    }

    for {
      r1 <- bpm.getProcessInfo(processId).map(_.managerId)
      r2 <- evalStringList(recipients, ctx)
      r3 <- evalString(subject, ctx)
      r4 <- evalString(message, ctx)
      r5 <- create(r1, r2, r3, r4)
    } yield r5
  }
}

object SendTaskOperationActor {
  def props(
    id: Operation.Id,
    bpm: Bpm,
    bpmnId: BpmnId,
    name: Operation.Name,
    loopCharacteristics: LoopCharacteristics,
    dataStructureId: Option[DataStructure.Id],
    plannedStart: Option[ZonedDateTime],
    plannedDuration: Option[FiniteDuration],
    in: List[Operation.BpmnId],
    out: List[Operation.BpmnId],
    recipients: List[Expression],
    subject: Expression,
    message: Expression,
    preAssignments: Assignments,
    postAssignments: Assignments,
    projectId: Project.Id,
    processId: Process.Id)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new SendTaskOperationActor(
      id = id,
      bpm = bpm,
      bpmnId = bpmnId,
      name = name,
      loopCharacteristics = loopCharacteristics,
      dataStructureId = dataStructureId,
      plannedStart = plannedStart,
      plannedDuration = plannedDuration,
      in = in,
      out = out,
      recipients = recipients,
      subject = subject,
      message = message,
      preAssignments = preAssignments,
      postAssignments = postAssignments,
      projectId = projectId,
      processId = processId))
  }
}

