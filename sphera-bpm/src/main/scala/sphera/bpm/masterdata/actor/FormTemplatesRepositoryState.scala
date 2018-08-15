package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime

import sphera.bpm.masterdata.actor.FormTemplatesRepositoryActor._
import sphera.bpm.masterdata.model.{ FormTemplate, ModifyAttr, UpdateFormTemplate }
import sphera.core.akkaext.actor.CqrsState

case class FormTemplatesRepositoryState(storage: Map[FormTemplate.Id, FormTemplate] = Map.empty) extends CqrsState {
  def create(x: UpdateFormTemplate): FormTemplatesRepositoryState = {
    if (storage.get(x.id).isDefined) throw new IllegalArgumentException
    copy(storage + (x.id -> FormTemplate(
      id = x.id,
      name = x.name,
      description = x.description,
      dataStructureId = x.dataStructureId,
      formStructure = x.formStructure,
      modifyAttr = ModifyAttr(x.userId, ZonedDateTime.now()))))
  }
  def update(x: UpdateFormTemplate): FormTemplatesRepositoryState = {
    val d = storage(x.id)
    copy(storage + (x.id -> d.copy(
      name = x.name,
      description = x.description,
      dataStructureId = x.dataStructureId,
      formStructure = x.formStructure,
      modifyAttr = d.modifyAttr.copy(updatedBy = Option(x.userId), updatedOn = Option(ZonedDateTime.now())))))
  }
  def delete(id: FormTemplate.Id): FormTemplatesRepositoryState = copy(storage - id)
  def exists(id: FormTemplate.Id): Boolean = storage.get(id).isDefined
  def getById(id: FormTemplate.Id): Option[FormTemplate] = storage.get(id)
  def getAll: Map[FormTemplate.Id, FormTemplate] = storage
  def update = {
    case CreatedEvt(x) => create(x)
    case UpdatedEvt(x) => update(x)
    case DeletedEvt(x) => delete(x)
  }
}