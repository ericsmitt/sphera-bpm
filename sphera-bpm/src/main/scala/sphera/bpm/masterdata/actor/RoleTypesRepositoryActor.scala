package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.Props
import sphera.bpm.masterdata.model.RoleType.RoleTypes
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._

class RoleTypesRepositoryActor(val id: Repository.Id, val initState: RoleTypesState) extends CqrsPersistentActor[RoleTypesState] {
  def create(state: RoleTypesState, entry: UpdateRoleType): Unit = {
    if (state.exists(entry.id)) sender ! RoleTypesRepositoryActor.AlreadyExists
    else {
      persist(RoleTypesRepositoryActor.CreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! RoleTypesRepositoryActor.Done
      }
    }
  }
  def update(state: RoleTypesState, entry: UpdateRoleType): Unit = {
    if (state.exists(entry.id)) {
      persist(RoleTypesRepositoryActor.UpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! RoleTypesRepositoryActor.Done
      }
    } else sender ! RoleTypesRepositoryActor.NotFound
  }

  def delete(state: RoleTypesState, id: UUID): Unit = {
    if (state.exists(id)) {
      persist(RoleTypesRepositoryActor.DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! RoleTypesRepositoryActor.Done
      }
    } else sender ! RoleTypesRepositoryActor.NotFound
  }

  def findById(state: RoleTypesState, id: UUID): Unit =
    sender ! RoleTypesRepositoryActor.RoleTypeOpt(state.getById(id))

  def findAll(state: RoleTypesState): Unit =
    sender ! RoleTypesRepositoryActor.RoleTypeMap(state.getAll)

  def behavior(state: RoleTypesState): Receive = {
    case RoleTypesRepositoryActor.CreateCmd(x) => create(state, x)
    case RoleTypesRepositoryActor.UpdateCmd(x) => update(state, x)
    case RoleTypesRepositoryActor.DeleteCmd(x) => delete(state, x)
    case RoleTypesRepositoryActor.GetById(x) => findById(state, x)
    case RoleTypesRepositoryActor.GetAll => findAll(state)
  }
}

object RoleTypesRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: UpdateRoleType) extends Command
  case class UpdateCmd(x: UpdateRoleType) extends Command
  case class DeleteCmd(id: RoleType.Id) extends Command

  case class GetById(id: RoleType.Id) extends Request
  object GetAll extends Request

  case class CreatedEvt(x: UpdateRoleType) extends Event
  case class UpdatedEvt(x: UpdateRoleType) extends Event
  case class DeletedEvt(id: RoleType.Id) extends Event

  object Done extends Response
  case class RoleTypeOpt(x: Option[RoleType]) extends Response
  case class RoleTypeMap(x: RoleTypes) extends Response
  object AlreadyExists extends Response
  object NotFound extends Response

  def props(id: Repository.Id, state: RoleTypesState = RoleTypesState()) =
    Props(new RoleTypesRepositoryActor(id, state))
}

