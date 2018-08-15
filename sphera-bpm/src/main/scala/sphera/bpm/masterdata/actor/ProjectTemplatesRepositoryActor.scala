package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.Props
import sphera.bpm.masterdata.model.ProjectTemplate.ProjectTemplates
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._

class ProjectTemplatesRepositoryActor(val id: Repository.Id, val initState: ProjectTemplatesState) extends CqrsPersistentActor[ProjectTemplatesState] {
  def create(state: State, entry: UpdateProjectTemplate): Unit = {
    if (state.exists(entry.id)) sender ! ProjectTemplatesRepositoryActor.AlreadyExists
    else {
      persist(ProjectTemplatesRepositoryActor.CreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! ProjectTemplatesRepositoryActor.Done
      }
    }
  }
  def update(state: State, entry: UpdateProjectTemplate): Unit = {
    if (state.exists(entry.id)) {
      persist(ProjectTemplatesRepositoryActor.UpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! ProjectTemplatesRepositoryActor.Done
      }
    } else sender ! ProjectTemplatesRepositoryActor.NotFound
  }

  def delete(state: State, id: UUID): Unit = {
    if (state.exists(id)) {
      persist(ProjectTemplatesRepositoryActor.DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! ProjectTemplatesRepositoryActor.Done
      }
    } else sender ! ProjectTemplatesRepositoryActor.NotFound
  }

  def findById(state: State, id: UUID): Unit =
    sender ! ProjectTemplatesRepositoryActor.ProjectTemplateOpt(state.getById(id))

  def findAll(state: State): Unit =
    sender ! ProjectTemplatesRepositoryActor.ProjectTemplateMap(state.getAll)

  def behavior(state: State): Receive = {
    case ProjectTemplatesRepositoryActor.CreateCmd(x) => create(state, x)
    case ProjectTemplatesRepositoryActor.UpdateCmd(x) => update(state, x)
    case ProjectTemplatesRepositoryActor.DeleteCmd(x) => delete(state, x)
    case ProjectTemplatesRepositoryActor.GetById(x) => findById(state, x)
    case ProjectTemplatesRepositoryActor.GetAll => findAll(state)
  }
}

object ProjectTemplatesRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: UpdateProjectTemplate) extends Command
  case class UpdateCmd(x: UpdateProjectTemplate) extends Command
  case class DeleteCmd(id: ProjectTemplate.Id) extends Command

  case class GetById(id: ProjectTemplate.Id) extends Request
  object GetAll extends Request

  case class CreatedEvt(x: UpdateProjectTemplate) extends Event
  case class UpdatedEvt(x: UpdateProjectTemplate) extends Event
  case class DeletedEvt(id: ProjectTemplate.Id) extends Event

  object Done extends Response
  case class ProjectTemplateOpt(x: Option[ProjectTemplate]) extends Response
  case class ProjectTemplateMap(x: ProjectTemplates) extends Response
  object AlreadyExists extends Response
  object NotFound extends Response

  def props(id: Repository.Id, state: ProjectTemplatesState = ProjectTemplatesState()) =
    Props(new ProjectTemplatesRepositoryActor(id, state))
}

