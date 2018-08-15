package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime

import sphera.bpm.masterdata.actor.DataStructuresRepositoryActor._
import sphera.bpm.masterdata.model.{ DataStructure, ModifyAttr, UpdateDataStructure }
import sphera.core.akkaext.actor.CqrsState

case class DataStructuresRepositoryState(storage: Map[DataStructure.Id, DataStructure] = Map.empty) extends CqrsState {
  def create(x: UpdateDataStructure): DataStructuresRepositoryState = {
    copy(storage + (x.id -> DataStructure(
      id = x.id,
      name = x.name,
      description = x.description,
      elements = x.elements,
      mutable = x.mutable,
      modifyAttr = ModifyAttr(x.userId, ZonedDateTime.now()))))
  }
  def update(x: UpdateDataStructure): DataStructuresRepositoryState = {
    val d = storage(x.id)
    copy(storage + (x.id -> d.copy(
      name = x.name,
      description = x.description,
      elements = x.elements,
      mutable = x.mutable,
      modifyAttr = d.modifyAttr.copy(updatedBy = Option(x.userId), updatedOn = Option(ZonedDateTime.now())))))
  }
  def delete(id: DataStructure.Id): DataStructuresRepositoryState = copy(storage - id)
  def exists(id: DataStructure.Id): Boolean = storage.get(id).isDefined
  def getById(id: DataStructure.Id): Option[DataStructure] = storage.get(id)
  def getAll: Map[DataStructure.Id, DataStructure] = storage

  def update: Update = {
    case CreatedEvt(x) => create(x)
    case UpdatedEvt(x) => update(x)
    case DeletedEvt(x) => delete(x)
  }
}