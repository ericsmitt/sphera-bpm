package sphera.bpm.masterdata.model

import java.util.UUID

import sphera.bpm.runnable.RunnableTemplate
import sphera.core.domain.tenancy.model.User

object ProjectTemplate {
  type Id = UUID
  type ProjectTemplates = Map[ProjectTemplate.Id, ProjectTemplate]
}

case class ProjectTemplate(
  id: ProjectTemplate.Id,
  name: String,
  description: Option[String],
  dataStructureId: DataStructure.Id,
  modifyAttr: ModifyAttr) extends RunnableTemplate

case class UpdateProjectTemplate(
  id: ProjectTemplate.Id,
  name: String,
  description: Option[String],
  dataStructureId: DataStructure.Id,
  userId: User.Id)

