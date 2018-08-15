package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime
import java.util.UUID

import sphera.bpm.masterdata.model.{ FileType, ModifyAttr, UpdateFileType }
import sphera.core.akkaext.actor.CqrsState

case class FileTypesState(storage: Map[UUID, FileType] = Map.empty) extends CqrsState {

  def create(c: UpdateFileType) = {
    if (storage.get(c.id).isDefined) throw new IllegalArgumentException
    copy(
      storage + (c.id -> FileType(
        id = c.id,
        name = c.name,
        description = c.description,
        extensions = c.extensions,
        isMultiple = c.isMultiple,
        classification = c.classification,
        modifyAttr = ModifyAttr(c.userId, ZonedDateTime.now()))))
  }

  def update1(c: UpdateFileType) = {
    if (storage.get(c.id).isEmpty) throw new IllegalArgumentException
    val s = storage(c.id)
    copy(
      storage + (c.id -> FileType(
        id = c.id,
        name = c.name,
        description = c.description,
        extensions = c.extensions,
        isMultiple = c.isMultiple,
        classification = c.classification,
        modifyAttr = s.modifyAttr.copy(updatedBy = Option(c.userId), updatedOn = Option(ZonedDateTime.now())))))
  }

  def delete(id: FileType.Id) = {
    FileTypesState(storage - id)
  }

  def resetUpdateCounter = {
    FileTypesState(storage)
  }

  def exists(id: FileType.Id) = storage.get(id).isDefined
  def getById(id: FileType.Id) = storage.get(id)
  def getAll = storage

  def update = {
    case FileTypesRepositoryActor.CreatedEvt(x) => create(x)
    case FileTypesRepositoryActor.UpdatedEvt(x) => update1(x)
    case FileTypesRepositoryActor.DeletedEvt(x) => delete(x)
  }

}

