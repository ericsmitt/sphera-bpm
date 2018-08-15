package sphera.bpm.masterdata.actor

import java.time.ZonedDateTime
import java.util.UUID

import sphera.bpm.masterdata.model.{ CalendarType, ModifyAttr, UpdateCalendarType }
import sphera.core.akkaext.actor.CqrsState
import sphera.core.domain.tenancy.model.User

case class CalendarTypesState(storage: Map[UUID, CalendarType] = Map.empty, usersCalendarTypeMap: Map[User.Id, CalendarType.Id] = Map.empty)
  extends CqrsState {

  def create(c: UpdateCalendarType) = {
    if (storage.get(c.id).isDefined) throw new IllegalArgumentException
    copy(
      storage + (c.id -> CalendarType(
        id = c.id,
        name = c.name,
        description = c.description,
        deviations = c.deviations,
        modifyAttr = ModifyAttr(c.userId, ZonedDateTime.now()))), usersCalendarTypeMap)
  }

  def update1(c: UpdateCalendarType) = {
    if (storage.get(c.id).isEmpty) throw new IllegalArgumentException
    val s = storage(c.id)
    copy(
      storage + (c.id -> CalendarType(
        id = c.id,
        name = c.name,
        description = c.description,
        deviations = c.deviations,
        modifyAttr = s.modifyAttr.copy(updatedBy = Option(c.userId), updatedOn = Option(ZonedDateTime.now())))), usersCalendarTypeMap)
  }

  def updateUsersCalendarTypeMap(userId: User.Id, calendarId: CalendarType.Id) = {
    copy(
      storage,
      usersCalendarTypeMap + (userId -> calendarId))
  }

  def delete(id: CalendarType.Id) = {
    CalendarTypesState(storage - id, refreshUsersCalendarTypeMap(id))
  }

  def refreshUsersCalendarTypeMap(id: CalendarType.Id) = {
    usersCalendarTypeMap.filterNot(v => v._2 == id)
  }

  def resetUpdateCounter = {
    CalendarTypesState(storage, usersCalendarTypeMap)
  }

  def exists(id: CalendarType.Id) = storage.get(id).isDefined
  def getById(id: CalendarType.Id) = storage.get(id)
  def getAll = storage
  def getUsersCalendarTypeMap = usersCalendarTypeMap
  def getUsersCalendarTypeMapById(id: User.Id) = usersCalendarTypeMap.get(id)

  def update = {
    case CalendarTypesRepositoryActor.CreatedEvt(x) => create(x)
    case CalendarTypesRepositoryActor.UpdatedEvt(x) => update1(x)
    case CalendarTypesRepositoryActor.UpdatedUsersCalendarTypeEvt(x, y) => updateUsersCalendarTypeMap(x, y)
    case CalendarTypesRepositoryActor.DeletedEvt(x) => delete(x)
  }
}

