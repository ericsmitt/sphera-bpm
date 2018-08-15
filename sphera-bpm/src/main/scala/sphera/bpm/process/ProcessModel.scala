package sphera.bpm.process

import java.time.ZonedDateTime

import sphera.bpm.bpmn.BpmnId
import sphera.bpm.masterdata.model.ProcessTemplate
import sphera.bpm.model._
import sphera.bpm.process.actor.ProcessState
import sphera.bpm.project._
import sphera.bpm.runnable._
import sphera.bpm.storage.definition.DefStorage
import sphera.core.domain.tenancy.model.User
import io.circe.Json

case class ProcessDef(defStorage: DefStorage, opDefs: Set[OperationDef])

object ProcessDef {
  def empty: ProcessDef = ProcessDef(defStorage = DefStorage.emptyRootDef(), opDefs = Set.empty)
}

case class ProcessExtendedInfo(info: ProcessInfo, state: ProcessState) extends RunnableExtendedInfo

case class ProcessInfo(
  id: Process.Id,
  name: String,
  description: Option[String],
  status: RunnableStatus,
  bpmnId: BpmnId,
  parentId: Option[Process.Id],
  childIds: Set[Process.Id],
  activeChildIds: Set[Process.Id],
  projectId: Project.Id,
  operationIds: Set[Operation.Id],
  relaredOperationId: Option[Operation.Id],
  relatedOperationBpmnId: Option[Operation.BpmnId],
  initiatorId: Option[Operation.Id],
  templateId: ProcessTemplate.Id,
  managerId: User.Id,
  startedBy: Option[Modifier],
  startedOn: Option[ZonedDateTime],
  modifyAttr: ModifyAttr) extends RunnableInfo

case class CreateProcessInfo(
  name: String,
  description: Option[String],
  data: Json,
  projectId: Project.Id,
  managerId: User.Id,
  templateId: ProcessTemplate.Id,
  parentId: Option[Process.Id] = None,
  relatedOperationId: Option[Operation.Id],
  relatedOperationBpmnId: Option[Operation.BpmnId],
  initiatorId: Option[Operation.Id] = None,
  initState: Option[ProcessState] = None) extends CreateRunnableInfo

case class UpdateProcessInfo(
  id: Process.Id,
  name: Option[String] = None,
  description: Option[Option[String]] = None,
  status: Option[RunnableStatus] = None,
  childIds: Option[Set[Process.Id]] = None,
  activeChildIds: Option[Set[Process.Id]] = None,
  operationIds: Option[Set[Operation.Id]] = None,
  managerId: Option[User.Id] = None,
  startedBy: Option[Option[Modifier]] = None,
  startedOn: Option[Option[ZonedDateTime]] = None) extends UpdateRunnableInfo