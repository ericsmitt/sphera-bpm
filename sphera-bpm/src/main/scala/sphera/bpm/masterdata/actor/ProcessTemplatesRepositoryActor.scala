package sphera.bpm.masterdata.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.{ Bpm, BpmnParserExceptions, OperationTemplateDataException }
import sphera.bpm.bpmn.{ BpmnParser, BpmnParserResult }
import sphera.bpm.masterdata.actor.ProcessTemplatesRepositoryActor._
import sphera.bpm.masterdata.model.ProcessTemplate.ProcessTemplates
import sphera.bpm.masterdata.model._
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._
import sphera.core.domain.tenancy.model.User

import scala.concurrent.ExecutionContext

class ProcessTemplatesRepositoryActor(
  val id: Repository.Id,
  val bpm: Bpm,
  val initState: ProcessTemplatesRepositoryState)(implicit val c: ExecutionContext, val t: Timeout)
  extends CqrsPersistentActor[ProcessTemplatesRepositoryState] with BpmnParser {

  def createProcessTemplate(state: ProcessTemplatesRepositoryState, update: UpdateProcessTemplate): Unit = {
    if (state.processTemplateExists(update.id)) sender ! AlreadyExists else {
      after(parse(update.sourceCode, update.opData)) { (response, state) =>
        response match {
          case Right(BpmnParserResult(bpmnId, opDefs)) =>
            val x1 = update.opData.map(_.bpmnId)
            val x2 = opDefs.map(_.bpmnId)
            if (x1 == x2) {
              val x3 = update.copy(opDefs = opDefs)
              persist(state, CreatedProcessTemplateEvt(x3)) { (state, event) =>
                sender ! Done
              }
            } else {
              val x = x1 diff x2
              sender() ! InvalidOperationTemplateData(OperationTemplateDataException(x))
            }
          case Left(exceptions) =>
            sender() ! InvalidTemplate(BpmnParserExceptions(exceptions))
        }
      }
    }
  }

  def updateProcessTemplate(state: ProcessTemplatesRepositoryState, x: UpdateProcessTemplate): Unit = {
    if (state.processTemplateExists(x.id)) {
      persist(UpdatedProcessTemplateEvt(x)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotFound
  }

  def deleteProcessTemplate(state: ProcessTemplatesRepositoryState, id: ProcessTemplate.Id): Unit = {
    if (state.processTemplateExists(id)) {
      persist(DeletedProcessTemplateEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! NotFound
  }

  def behavior(state: ProcessTemplatesRepositoryState): Receive = {
    case CreateProcessTemplateCmd(x) => createProcessTemplate(state, x)
    case UpdateProcessTemplateCmd(x) => updateProcessTemplate(state, x)
    case DeleteProcessTemplateCmd(x) => deleteProcessTemplate(state, x)
    case GetProcessTemplateById(x) => sender ! ProcessTemplateOpt(state.getProcessTemplateById(x))
    case GetProcessTemplateAll => sender ! ProcessTemplateMap(state.getAll)
  }
}

object ProcessTemplatesRepositoryActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateProcessTemplateCmd(x: UpdateProcessTemplate) extends Command
  case class UpdateProcessTemplateCmd(x: UpdateProcessTemplate) extends Command
  case class DeleteProcessTemplateCmd(id: ProcessTemplate.Id) extends Command

  case class GetProcessTemplateById(id: ProcessTemplate.Id) extends Request
  case object GetProcessTemplateAll extends Request

  case class CreatedProcessTemplateEvt(x: UpdateProcessTemplate) extends Event
  case class UpdatedProcessTemplateEvt(x: UpdateProcessTemplate) extends Event
  case class DeletedProcessTemplateEvt(id: ProcessTemplate.Id) extends Event

  case object Done extends Response
  case class ProcessTemplateOpt(x: Option[ProcessTemplate]) extends Response
  case class ProcessTemplateMap(x: ProcessTemplates) extends Response
  case class InvalidOperationTemplateData(exception: OperationTemplateDataException) extends Response
  case class InvalidTemplate(exception: BpmnParserExceptions) extends Response
  case object AlreadyExists extends Response
  case object NotFound extends Response

  def props(
    id: Repository.Id,
    bpm: Bpm,
    state: ProcessTemplatesRepositoryState = ProcessTemplatesRepositoryState())(implicit c: ExecutionContext, t: Timeout) = {
    Props(new ProcessTemplatesRepositoryActor(id, bpm, state))
  }
}