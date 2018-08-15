package sphera.bpm.process

import java.time.ZonedDateTime

import sphera.bpm.bpmn.SequenceFlow
import sphera.bpm.lang.{ Assignments, Expression }
import sphera.bpm.masterdata.model.{ DataStructure, FormTemplate, ProcessTemplate }
import sphera.bpm.process.Operation._
import sphera.core.akkaext.actor.RawPath
import sphera.core.domain.tenancy.model.User
import io.circe.Json

import scala.concurrent.duration.FiniteDuration

sealed trait OperationDef {
  def bpmnId: Operation.BpmnId
  def name: Operation.Name
}

sealed trait OperationTemplateData {
  def bpmnId: Operation.BpmnId
}

case class StartOperationDef(
  bpmnId: Operation.BpmnId,
  name: Operation.Name,
  out: List[Operation.BpmnId],
  assignments: Assignments = List.empty) extends OperationDef

case class StartOperationTemplateData(
  bpmnId: Operation.BpmnId,
  assignments: Assignments = List.empty) extends OperationTemplateData

case class EndOperationDef(
  bpmnId: Operation.BpmnId,
  name: Operation.Name,
  in: List[Operation.BpmnId],
  assignments: Assignments = List.empty) extends OperationDef

case class EndOperationTemplateData(
  bpmnId: Operation.BpmnId,
  assignments: Assignments = List.empty) extends OperationTemplateData

case class ParallelGatewayOperationDef(
  bpmnId: Operation.BpmnId,
  name: Operation.Name,
  in: List[Operation.BpmnId],
  out: List[Operation.BpmnId],
  assignments: Assignments = List.empty) extends OperationDef

case class ParallelGatewayOperationTemplateData(
  bpmnId: Operation.BpmnId,
  assignments: Assignments = List.empty) extends OperationTemplateData

case class ExclusiveGatewayOperationDef(
  bpmnId: Operation.BpmnId,
  name: Operation.Name,
  in: List[Operation.BpmnId],
  out: List[Operation.BpmnId],
  conditions: List[Branch],
  assignments: Assignments = List.empty) extends OperationDef

case class ExclusiveGatewayOperationTemplateData(
  bpmnId: Operation.BpmnId,
  sequenceflowExpr: Map[SequenceFlow.Id, Expression],
  assignments: Assignments = List.empty) extends OperationTemplateData

case class InclusiveGatewayOperationDef(
  bpmnId: Operation.BpmnId,
  name: Operation.Name,
  conditions: List[Branch]) extends OperationDef

case class InclusiveGatewayOperationTemplateData(
  bpmnId: Operation.BpmnId,
  conditions: List[Branch]) extends OperationTemplateData

sealed trait Branch {
  val desc: Operation.Description
  val operation: Operation.BpmnId
}

case class CaseBranch(
  desc: Operation.Description,
  expr: Expression,
  operation: Operation.BpmnId) extends Branch

case class OtherwiseBranch(desc: Operation.Description, operation: Operation.BpmnId) extends Branch

case class ScriptTaskOperationDef(
  bpmnId: Operation.BpmnId,
  name: Operation.Name,
  in: List[Operation.BpmnId],
  out: List[Operation.BpmnId],
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationDef

case class ScriptTaskOperationTemplateData(
  bpmnId: Operation.BpmnId,
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationTemplateData

case class SendTaskOperationDef(
  bpmnId: BpmnId,
  name: Name,
  recipients: List[Expression],
  subject: Expression,
  message: Expression,
  in: List[Operation.BpmnId],
  out: List[Operation.BpmnId],
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationDef

case class SendTaskOperationTemplateData(
  bpmnId: Operation.BpmnId,
  recipients: List[Expression],
  subject: Expression,
  message: Expression,
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationTemplateData

case class Assignment(path: RawPath, expr: Expression)

case class UserTaskOperationDef(
  bpmnId: BpmnId,
  name: Name,
  role: String,
  assigneeId: Option[User.Id],
  reviewerId: Option[User.Id],
  watcherIds: Set[User.Id],
  formTemplateId: FormTemplate.Id,
  dataStructureId: DataStructure.Id,
  plannedStart: Option[ZonedDateTime],
  plannedDuration: Option[FiniteDuration],
  in: List[Operation.BpmnId],
  out: List[Operation.BpmnId],
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationDef

case class UserTaskOperationTemplateData(
  bpmnId: Operation.BpmnId,
  role: String,
  assigneeId: Option[User.Id],
  reviewerId: Option[User.Id],
  watcherIds: Set[User.Id],
  formTemplateId: FormTemplate.Id,
  dataStructureId: DataStructure.Id,
  plannedStart: Option[ZonedDateTime],
  plannedDuration: Option[FiniteDuration],
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationTemplateData

case class SubProcessOperationDef(
  bpmnId: Operation.BpmnId,
  name: Operation.Name,
  data: Json,
  templateId: ProcessTemplate.Id,
  taskDataStructureId: Option[DataStructure.Id],
  plannedStart: Option[ZonedDateTime],
  plannedDuration: Option[FiniteDuration],
  in: List[Operation.BpmnId],
  out: List[Operation.BpmnId],
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationDef

case class SubProcessOperationTemplateData(
  bpmnId: Operation.BpmnId,
  data: Json,
  templateId: ProcessTemplate.Id,
  taskDataStructureId: Option[DataStructure.Id],
  plannedStart: Option[ZonedDateTime],
  plannedDuration: Option[FiniteDuration],
  preAssignments: Assignments,
  postAssignments: Assignments) extends OperationTemplateData
