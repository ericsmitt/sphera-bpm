package sphera.bpm.masterdata.actor

import akka.actor.Props
import sphera.bpm.masterdata.actor.DataStructuresRepositoryActor._
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._

class DataStructuresRepositoryActor(val id: Repository.Id, val initState: DataStructuresRepositoryState) extends CqrsPersistentActor[DataStructuresRepositoryState] {
  def create(state: DataStructuresRepositoryState, x: UpdateDataStructure): Unit = {
    if (state.exists(x.id)) sender ! AlreadyExists else {
      persist(CreatedEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def update(state: DataStructuresRepositoryState, x: UpdateDataStructure): Unit = {
    if (state.exists(x.id)) {
      persist(UpdatedEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotFound
  }

  def delete(state: DataStructuresRepositoryState, id: DataStructure.Id): Unit = {
    if (state.exists(id)) {
      persist(DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotFound
  }

  def findById(state: DataStructuresRepositoryState, id: DataStructure.Id): Unit =
    sender ! DataStructureOpt(state.getById(id))

  def findAll(state: DataStructuresRepositoryState): Unit =
    sender ! DataStructureMap(state.getAll)

  def behavior(state: DataStructuresRepositoryState): Receive = {
    case CreateCmd(x) => create(state, x)
    case UpdateCmd(x) => update(state, x)
    case DeleteCmd(x) => delete(state, x)
    case GetById(x) => findById(state, x)
    case GetAll => findAll(state)
  }
}

object DataStructuresRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: UpdateDataStructure) extends Command
  case class UpdateCmd(x: UpdateDataStructure) extends Command
  case class DeleteCmd(id: DataStructure.Id) extends Command

  case class GetById(id: DataStructure.Id) extends Request
  case object GetAll extends Request

  case class CreatedEvt(x: UpdateDataStructure) extends Event
  case class UpdatedEvt(x: UpdateDataStructure) extends Event
  case class DeletedEvt(id: DataStructure.Id) extends Event

  case object Done extends Response
  case class DataStructureOpt(x: Option[DataStructure]) extends Response
  case class DataStructureMap(x: Map[DataStructure.Id, DataStructure]) extends Response
  case object AlreadyExists extends Response
  case object NotFound extends Response

  def props(id: Repository.Id, state: DataStructuresRepositoryState = DataStructuresRepositoryState()) =
    Props(new DataStructuresRepositoryActor(id, state))
}