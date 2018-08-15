package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime

import sphera.bpm.masterdata.model.ProjectTemplate.ProjectTemplates
import sphera.bpm.masterdata.model.{ ModifyAttr, ProjectTemplate, UpdateProjectTemplate }
import sphera.core.akkaext.actor.CqrsState

case class ProjectTemplatesState(storage: ProjectTemplates = Map.empty) extends CqrsState {
  def create(c: UpdateProjectTemplate): ProjectTemplatesState = {
    if (storage.get(c.id).isDefined) throw new IllegalArgumentException
    copy(
      storage + (c.id -> ProjectTemplate(
        id = c.id,
        name = c.name,
        description = c.description,
        dataStructureId = c.dataStructureId,
        modifyAttr = ModifyAttr(c.userId, ZonedDateTime.now()))))
  }

  def update1(c: UpdateProjectTemplate): ProjectTemplatesState = {
    if (storage.get(c.id).isEmpty) throw new IllegalArgumentException
    val s = storage(c.id)
    copy(
      storage + (c.id -> ProjectTemplate(
        id = c.id,
        name = c.name,
        description = c.description,
        dataStructureId = c.dataStructureId,
        modifyAttr = s.modifyAttr.copy(updatedBy = Option(c.userId), updatedOn = Option(ZonedDateTime.now())))))
  }

  def delete(id: ProjectTemplate.Id): ProjectTemplatesState = {
    ProjectTemplatesState(storage - id)
  }

  def resetUpdateCounter: ProjectTemplatesState = {
    ProjectTemplatesState(storage)
  }

  def exists(id: ProjectTemplate.Id): Boolean = storage.get(id).isDefined
  def getById(id: ProjectTemplate.Id): Option[ProjectTemplate] = storage.get(id)
  def getAll: ProjectTemplates = storage

  def update = {
    case ProjectTemplatesRepositoryActor.CreatedEvt(x) => create(x)
    case ProjectTemplatesRepositoryActor.UpdatedEvt(x) => update1(x)
    case ProjectTemplatesRepositoryActor.DeletedEvt(x) => delete(x)
  }
}

