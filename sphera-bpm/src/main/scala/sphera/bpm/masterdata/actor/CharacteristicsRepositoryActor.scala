package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.Props
import sphera.bpm.masterdata.model.Characteristic.Characteristics
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._

class CharacteristicsRepositoryActor(val id: Repository.Id, val initState: CharacteristicState) extends CqrsPersistentActor[CharacteristicState] {
  def create(state: CharacteristicState, entry: UpdateCharacteristic): Unit = {
    if (state.exists(entry.id)) sender ! CharacteristicsRepositoryActor.AlreadyExists
    else {
      persist(CharacteristicsRepositoryActor.CreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! CharacteristicsRepositoryActor.Done
      }
    }
  }
  def update(state: CharacteristicState, entry: UpdateCharacteristic): Unit = {
    if (state.exists(entry.id)) {
      persist(CharacteristicsRepositoryActor.UpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! CharacteristicsRepositoryActor.Done
      }
    } else sender ! CharacteristicsRepositoryActor.NotFound
  }

  def delete(state: CharacteristicState, id: UUID): Unit = {
    if (state.exists(id)) {
      persist(CharacteristicsRepositoryActor.DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! CharacteristicsRepositoryActor.Done
      }
    } else sender ! CharacteristicsRepositoryActor.NotFound
  }

  def findById(state: CharacteristicState, id: UUID): Unit =
    sender ! CharacteristicsRepositoryActor.CharacteristicOpt(state.getById(id))

  def findAll(state: CharacteristicState): Unit =
    sender ! CharacteristicsRepositoryActor.CharacteristicMap(state.getAll)

  //  def clear(storage: CharacteristicState): Unit = {
  //    persist(CharacteristicRepositoryActor.ClearEvt()) { event =>
  //      changeState(storage.withStatus(event))
  //      sender ! CharacteristicRepositoryActor.Done
  //    }
  //  }

  def behavior(state: CharacteristicState): Receive = {
    case CharacteristicsRepositoryActor.CreateCmd(x) => create(state, x)
    case CharacteristicsRepositoryActor.UpdateCmd(x) => update(state, x)
    case CharacteristicsRepositoryActor.DeleteCmd(x) => delete(state, x)
    case CharacteristicsRepositoryActor.GetById(x) => findById(state, x)
    case CharacteristicsRepositoryActor.GetAll => findAll(state)
    //    case CharacteristicRepositoryActor.ClearCmd => clear(storage)
  }
}

object CharacteristicsRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: UpdateCharacteristic) extends Command
  case class UpdateCmd(x: UpdateCharacteristic) extends Command
  case class DeleteCmd(id: Characteristic.Id) extends Command

  case class GetById(id: Characteristic.Id) extends Request
  object GetAll extends Request

  case class CreatedEvt(x: UpdateCharacteristic) extends Event
  case class UpdatedEvt(x: UpdateCharacteristic) extends Event
  case class DeletedEvt(id: Characteristic.Id) extends Event

  object Done extends Response
  case class CharacteristicOpt(x: Option[Characteristic]) extends Response
  case class CharacteristicMap(x: Characteristics) extends Response
  object AlreadyExists extends Response
  object NotFound extends Response

  def props(id: Repository.Id, state: CharacteristicState = CharacteristicState()) = Props(new CharacteristicsRepositoryActor(id, state))
}

