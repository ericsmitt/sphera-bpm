package sphera.bpm.masterdata.actor

import java.util.UUID

import akka.actor.Props
import sphera.bpm.masterdata.model.FileType.FileTypes
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._

class FileTypesRepositoryActor(val id: Repository.Id, val initState: FileTypesState) extends CqrsPersistentActor[FileTypesState] {
  def create(state: FileTypesState, entry: UpdateFileType): Unit = {
    if (state.exists(entry.id)) sender ! FileTypesRepositoryActor.AlreadyExists
    else {
      persist(FileTypesRepositoryActor.CreatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! FileTypesRepositoryActor.Done
      }
    }
  }
  def update(state: FileTypesState, entry: UpdateFileType): Unit = {
    if (state.exists(entry.id)) {
      persist(FileTypesRepositoryActor.UpdatedEvt(entry)) { event =>
        changeState(state.updated(event))
        sender ! FileTypesRepositoryActor.Done
      }
    } else sender ! FileTypesRepositoryActor.NotFound
  }

  def delete(state: FileTypesState, id: UUID): Unit = {
    if (state.exists(id)) {
      persist(FileTypesRepositoryActor.DeletedEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! FileTypesRepositoryActor.Done
      }
    } else sender ! FileTypesRepositoryActor.NotFound
  }

  def findById(state: FileTypesState, id: UUID): Unit =
    sender ! FileTypesRepositoryActor.FileTypeOpt(state.getById(id))

  def findAll(state: FileTypesState): Unit =
    sender ! FileTypesRepositoryActor.FileTypeMap(state.getAll)

  def behavior(state: FileTypesState): Receive = {
    case FileTypesRepositoryActor.CreateCmd(x) => create(state, x)
    case FileTypesRepositoryActor.UpdateCmd(x) => update(state, x)
    case FileTypesRepositoryActor.DeleteCmd(x) => delete(state, x)
    case FileTypesRepositoryActor.GetById(x) => findById(state, x)
    case FileTypesRepositoryActor.GetAll => findAll(state)
  }
}

object FileTypesRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateCmd(x: UpdateFileType) extends Command
  case class UpdateCmd(x: UpdateFileType) extends Command
  case class DeleteCmd(id: FileType.Id) extends Command

  case class GetById(id: FileType.Id) extends Request
  object GetAll extends Request

  case class CreatedEvt(x: UpdateFileType) extends Event
  case class UpdatedEvt(x: UpdateFileType) extends Event
  case class DeletedEvt(id: FileType.Id) extends Event

  object Done extends Response
  case class FileTypeOpt(x: Option[FileType]) extends Response
  case class FileTypeMap(x: FileTypes) extends Response
  object AlreadyExists extends Response
  object NotFound extends Response

  def props(id: Repository.Id, state: FileTypesState = FileTypesState()) =
    Props(new FileTypesRepositoryActor(id, state))
}

