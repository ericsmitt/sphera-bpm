package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime

import sphera.bpm.masterdata.actor.ProcessTemplatesRepositoryActor._
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.CqrsState

case class ProcessTemplatesRepositoryState(storage: Map[ProcessTemplate.Id, ProcessTemplate] = Map.empty) extends CqrsState {
  def createProcessTemplate(create: UpdateProcessTemplate): ProcessTemplatesRepositoryState = {
    copy(storage + (create.id -> ProcessTemplate(
      id = create.id,
      bpmnId = create.bpmnId,
      name = create.name,
      description = create.description,
      sourceCode = create.sourceCode,
      opDefs = create.opDefs,
      opData = create.opData,
      dataStructureId = create.dataStructureId,
      classification = create.classification,
      modifyAttr = ModifyAttr(create.userId, ZonedDateTime.now()))))
  }

  def updateProcessTemplate(update: UpdateProcessTemplate): ProcessTemplatesRepositoryState = {
    val d = storage(update.id)
    copy(storage + (update.id -> d.copy(
      name = update.name,
      description = update.description,
      sourceCode = update.sourceCode,
      opDefs = update.opDefs,
      opData = update.opData,
      dataStructureId = update.dataStructureId,
      classification = update.classification,
      modifyAttr = d.modifyAttr.copy(updatedBy = Option(update.userId), updatedOn = Option(ZonedDateTime.now())))))
  }

  def deleteProcessTemplate(id: ProcessTemplate.Id): ProcessTemplatesRepositoryState = copy(storage - id)

  def processTemplateExists(id: ProcessTemplate.Id): Boolean = storage.get(id).isDefined

  def getProcessTemplateById(id: ProcessTemplate.Id): Option[ProcessTemplate] = storage.get(id)

  def getAll: Map[ProcessTemplate.Id, ProcessTemplate] = storage

  def update: Update = {
    case CreatedProcessTemplateEvt(x) => createProcessTemplate(x)
    case UpdatedProcessTemplateEvt(x) => updateProcessTemplate(x)
    case DeletedProcessTemplateEvt(x) => deleteProcessTemplate(x)
  }
}