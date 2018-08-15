package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.Props
import sphera.bpm.masterdata.model.CalendarType.CalendarTypes
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._
import sphera.core.domain.tenancy.model.User

class CalendarTypeRepositoryActor(val id: Repository.Id, val initState: CalendarTypesState)
  extends CqrsPersistentActor[CalendarTypesState] {

  def create(state: CalendarTypesState, entry: UpdateCalendarType): Unit = {
    if (state.exists(entry.id)) sender ! CalendarTypesRepositoryActor.AlreadyExists
    else {
      persist(CalendarTypesRepositoryActor.CreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! CalendarTypesRepositoryActor.Done
      }
    }
  }
  def update(state: CalendarTypesState, entry: UpdateCalendarType): Unit = {
    if (state.exists(entry.id)) {
      persist(CalendarTypesRepositoryActor.UpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! CalendarTypesRepositoryActor.Done
      }
    } else sender ! CalendarTypesRepositoryActor.NotFound
  }

  def delete(state: CalendarTypesState, id: UUID): Unit = {
    if (state.exists(id)) {
      persist(CalendarTypesRepositoryActor.DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! CalendarTypesRepositoryActor.Done
      }
    } else sender ! CalendarTypesRepositoryActor.NotFound
  }

  def findById(state: CalendarTypesState, id: UUID): Unit =
    sender ! CalendarTypesRepositoryActor.CalendarTypeOpt(state.getById(id))

  def findAll(state: CalendarTypesState): Unit =
    sender ! CalendarTypesRepositoryActor.CalendarTypeMap(state.getAll)

  def updateUsersCalendarTypeMap(state: CalendarTypesState, userId: User.Id, calendarId: CalendarType.Id): Unit = {
    persist(CalendarTypesRepositoryActor.UpdatedUsersCalendarTypeEvt(userId, calendarId)) { event =>
      changeState(state.updated(event))
      sender ! CalendarTypesRepositoryActor.Done

    }
  }

  def findUsersCalendarTypesMap(state: CalendarTypesState) = sender ! CalendarTypesRepositoryActor
    .UsersCalendarTypes(state.getUsersCalendarTypeMap)

  def findUsersCalendarTypeById(state: CalendarTypesState, id: UUID) = {
    state.getUsersCalendarTypeMapById(id) match {
      case Some(x) => sender ! CalendarTypesRepositoryActor.UsersCalendarType(id, x)
      case None => sender ! CalendarTypesRepositoryActor.NotFound
    }

  }

  def behavior(state: CalendarTypesState): Receive = {
    case CalendarTypesRepositoryActor.CreateCmd(x) => create(state, x)
    case CalendarTypesRepositoryActor.UpdateCmd(x) => update(state, x)
    case CalendarTypesRepositoryActor.DeleteCmd(x) => delete(state, x)
    case CalendarTypesRepositoryActor.GetById(x) => findById(state, x)
    case CalendarTypesRepositoryActor.GetAll => findAll(state)
    case CalendarTypesRepositoryActor.UpdateUsersCalendarTypesMapCmd(x, y) => updateUsersCalendarTypeMap(state, x, y)
    case CalendarTypesRepositoryActor.GetUsersCalendarType => findUsersCalendarTypesMap(state)
    case CalendarTypesRepositoryActor.GetUsersCalendarTypeId(x) => findUsersCalendarTypeById(state, x)
  }
}

object CalendarTypesRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: UpdateCalendarType) extends Command
  case class UpdateCmd(x: UpdateCalendarType) extends Command
  case class UpdateUsersCalendarTypesMapCmd(userId: User.Id, calendarId: CalendarType.Id) extends Command
  case class DeleteCmd(id: CalendarType.Id) extends Command

  case class GetById(id: CalendarType.Id) extends Request
  object GetAll extends Request
  object GetUsersCalendarType extends Request
  case class GetUsersCalendarTypeId(id: CalendarType.Id) extends Request

  case class CreatedEvt(x: UpdateCalendarType) extends Event
  case class UpdatedEvt(x: UpdateCalendarType) extends Event
  case class UpdatedUsersCalendarTypeEvt(x: User.Id, y: CalendarType.Id) extends Event
  case class DeletedEvt(id: CalendarType.Id) extends Event

  case object Done extends Response
  case class CalendarTypeOpt(x: Option[CalendarType]) extends Response
  case class CalendarTypeMap(x: CalendarTypes) extends Response
  case object AlreadyExists extends Response
  case object NotFound extends Response
  case class UsersCalendarType(userId: UUID, calendarId: UUID) extends Response
  case class UsersCalendarTypes(userCalendarTypeMap: Map[UUID, UUID]) extends Response

  def props(id: Repository.Id, state: CalendarTypesState = CalendarTypesState()) =
    Props(new CalendarTypeRepositoryActor(id, state))
}

