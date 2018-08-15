package sphera.bpm.project

import java.time.ZonedDateTime

import sphera.bpm.masterdata.model.ProjectTemplate
import sphera.bpm.model.{ Modifier, ModifyAttr }
import sphera.bpm.process.Operation
import sphera.bpm.project.actor.ProjectState
import sphera.bpm.runnable._
import sphera.bpm.storage.definition.DefStorage
import sphera.core.domain.tenancy.model.User
import io.circe.Json

case class ProjectDef(defStorage: DefStorage)

object ProjectDef {
  def empty: ProjectDef = ProjectDef(DefStorage.emptyRootDef())
}

case class ProjectExtendedInfo(info: ProjectInfo, state: ProjectState) extends RunnableExtendedInfo

case class ProjectInfo(
  id: Project.Id,
  name: String,
  description: Option[String],
  status: RunnableStatus,
  parentId: Option[Project.Id],
  childIds: Set[Project.Id],
  activeChildIds: Set[Project.Id],
  templateId: ProjectTemplate.Id,
  managerId: User.Id,
  relaredOperationId: Option[Operation.Id],
  relatedOperationBpmnId: Option[Operation.BpmnId],
  startedBy: Option[Modifier],
  startedOn: Option[ZonedDateTime],
  modifyAttr: ModifyAttr) extends RunnableInfo

case class CreateProjectInfo(
  name: String,
  description: Option[String],
  data: Json,
  parentId: Option[Project.Id],
  managerId: User.Id,
  relatedOperationId: Option[Operation.Id],
  relatedOperationBpmnId: Option[Operation.BpmnId],
  templateId: ProjectTemplate.Id) extends CreateRunnableInfo

case class UpdateProjectInfo(
  id: Project.Id,
  name: Option[String] = None,
  description: Option[Option[String]] = None,
  status: Option[RunnableStatus] = None,
  childIds: Option[Set[Project.Id]] = None,
  activeChildIds: Option[Set[Project.Id]] = None,
  managerId: Option[User.Id] = None,
  startedBy: Option[Option[Modifier]] = None,
  startedOn: Option[Option[ZonedDateTime]] = None) extends UpdateRunnableInfo
