package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime
import java.util.UUID

import sphera.bpm.masterdata.model.{ ModifyAttr, RoleType, UpdateRoleType }
import sphera.core.akkaext.actor.CqrsState

case class RoleTypesState(storage: Map[UUID, RoleType] = Map.empty) extends CqrsState {
  def create(c: UpdateRoleType) = {
    if (storage.get(c.id).isDefined) throw new IllegalArgumentException
    copy(
      storage + (c.id -> RoleType(
        id = c.id,
        name = c.name,
        description = c.description,
        users = c.users,
        classification = c.classification,
        modifyAttr = ModifyAttr(c.userId, ZonedDateTime.now()))))
  }

  def update1(c: UpdateRoleType) = {
    if (storage.get(c.id).isEmpty) throw new IllegalArgumentException
    val s = storage(c.id)
    copy(
      storage + (c.id -> RoleType(
        id = c.id,
        name = c.name,
        description = c.description,
        users = c.users,
        classification = c.classification,
        modifyAttr = s.modifyAttr.copy(updatedBy = Option(c.userId), updatedOn = Option(ZonedDateTime.now())))))
  }

  def delete(id: RoleType.Id) = {
    RoleTypesState(storage - id)
  }

  def resetUpdateCounter = {
    RoleTypesState(storage)
  }

  def exists(id: RoleType.Id) = storage.get(id).isDefined
  def getById(id: RoleType.Id) = storage.get(id)
  def getAll = storage

  def update = {
    case RoleTypesRepositoryActor.CreatedEvt(x) => create(x)
    case RoleTypesRepositoryActor.UpdatedEvt(x) => update1(x)
    case RoleTypesRepositoryActor.DeletedEvt(x) => delete(x)
  }

}

