package sphera.bpm.masterdata.actor

import akka.actor.Props
import sphera.bpm.masterdata.actor.FormTemplatesRepositoryActor._
import sphera.bpm.masterdata.model.FormTemplate.FormTemplates
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._

class FormTemplatesRepositoryActor(val id: Repository.Id, val initState: FormTemplatesRepositoryState) extends CqrsPersistentActor[FormTemplatesRepositoryState] {
  def create(state: FormTemplatesRepositoryState, x: UpdateFormTemplate): Unit = {
    if (state.exists(x.id)) sender ! AlreadyExists else {
      persist(CreatedEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    }
  }

  def update(state: FormTemplatesRepositoryState, x: UpdateFormTemplate): Unit = {
    if (state.exists(x.id)) {
      persist(UpdatedEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotFound
  }

  def delete(state: FormTemplatesRepositoryState, id: FormTemplate.Id): Unit = {
    if (state.exists(id)) {
      persist(DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotFound
  }

  def findById(state: FormTemplatesRepositoryState, id: FormTemplate.Id): Unit =
    sender ! FormTemplateOpt(state.getById(id))

  def findAll(state: FormTemplatesRepositoryState): Unit =
    sender ! FormTemplateMap(state.getAll)

  def behavior(state: FormTemplatesRepositoryState): Receive = {
    case CreateCmd(x) => create(state, x)
    case UpdateCmd(x) => update(state, x)
    case DeleteCmd(x) => delete(state, x)
    case GetById(x) => findById(state, x)
    case GetAll => findAll(state)
  }
}

object FormTemplatesRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: UpdateFormTemplate) extends Command
  case class UpdateCmd(x: UpdateFormTemplate) extends Command
  case class DeleteCmd(id: FormTemplate.Id) extends Command

  case class GetById(id: FormTemplate.Id) extends Request
  case object GetAll extends Request

  case class CreatedEvt(x: UpdateFormTemplate) extends Event
  case class UpdatedEvt(x: UpdateFormTemplate) extends Event
  case class DeletedEvt(id: FormTemplate.Id) extends Event

  case object Done extends Response
  case class FormTemplateOpt(x: Option[FormTemplate]) extends Response
  case class FormTemplateMap(x: FormTemplates) extends Response
  case object AlreadyExists extends Response
  case object NotFound extends Response

  def props(id: Repository.Id, state: FormTemplatesRepositoryState = FormTemplatesRepositoryState()) =
    Props(new FormTemplatesRepositoryActor(id, state))
}