package sphera.bpm.masterdata.model

import java.util.UUID

import sphera.bpm.process.{ OperationDef, OperationTemplateData, Process }
import sphera.bpm.runnable.RunnableTemplate
import sphera.core.domain.tenancy.model.User

case class ProcessTemplate(
  id: ProcessTemplate.Id,
  bpmnId: Process.BpmnId,
  name: String,
  description: Option[String],
  sourceCode: String,
  opDefs: Set[OperationDef],
  opData: Set[OperationTemplateData],
  dataStructureId: DataStructure.Id,
  classification: Map[Characteristic.Id, Characteristic.ValueId],
  modifyAttr: ModifyAttr) extends RunnableTemplate

case class UpdateProcessTemplate(
  id: ProcessTemplate.Id,
  bpmnId: Process.BpmnId,
  name: String,
  description: Option[String],
  sourceCode: String,
  opDefs: Set[OperationDef],
  opData: Set[OperationTemplateData],
  dataStructureId: DataStructure.Id,
  classification: Map[Characteristic.Id, Characteristic.ValueId],
  userId: User.Id)

object ProcessTemplate {
  type Id = UUID
  type ProcessTemplates = Map[ProcessTemplate.Id, ProcessTemplate]
}