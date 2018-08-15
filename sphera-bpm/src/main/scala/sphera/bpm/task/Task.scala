package sphera.bpm.task

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.bpm.process.Process
import sphera.bpm.runnable.RunnableLike
import sphera.bpm.task.Task.{ FormAlreadySubmitted, FormSubmit, Response, SubmitFormCmd }
import sphera.bpm.task.actor.{ ScriptTaskState, SendTaskState, UserTaskState }
import sphera.core.akkaext.actor.{ ActorId, CqrsCommand, CqrsEvent, CqrsResponse }
import io.circe.Json

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }

trait Task extends RunnableLike with TaskTypes {
  val tag: ClassTag[State]
  val ctag7 = tag
}

class UserTask(val actorRef: ActorRef)(implicit val c: ExecutionContext, val t: Timeout, val tag: ClassTag[UserTaskState])
  extends Task {

  type State = UserTaskState

  def submitForm(data: Json): Future[Task.Id] = {
    actorRef.ask(SubmitFormCmd(data))
      .mapTo[Response]
      .map {
        case FormSubmit(Success(x)) => x
        case FormSubmit(Failure(x)) => sys.error(x.getMessage)
        case FormAlreadySubmitted => sys.error("Form already submitted")
      }
  }
}

class SendTask(val actorRef: ActorRef)(implicit val c: ExecutionContext, val t: Timeout, val tag: ClassTag[SendTaskState])
  extends Task {

  type State = SendTaskState
}

class ScriptTask(val actorRef: ActorRef)(implicit val c: ExecutionContext, val t: Timeout, val tag: ClassTag[ScriptTaskState])
  extends Task {

  type State = ScriptTaskState
}

object Task {
  type Id = ActorId
  type RawId = String
  type BpmnId = sphera.bpm.bpmn.BpmnId

  trait Command extends CqrsCommand
  trait Event extends CqrsEvent
  trait Response extends CqrsResponse

  case class SubmitFormCmd(data: Json) extends Command
  case class StartProcessCmd(processId: Process.Id, data: Json) extends Command
  case class StartProjectCmd(processId: Process.Id, data: Json) extends Command
  case object CompleteSubProcessCmd extends Command

  case class SubmitFormEvt(data: Json) extends Event

  case object FormAlreadySubmitted extends Response
  case class FormSubmit(taskId: Try[Task.Id]) extends Response
  case object Done extends Response
}
