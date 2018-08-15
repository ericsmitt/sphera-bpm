package sphera.bpm.task.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.bpm.task.Task
import sphera.bpm.task.Task._
import sphera.core.akkaext.actor.CqrsEvent
import io.circe.Json

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag
import scala.util.{ Failure, Success, Try }

class UserTaskActor(
  val id: Task.Id,
  val bpm: Bpm,
  val initState: UserTaskState)(implicit val c: ExecutionContext, val t: Timeout, val tag: ClassTag[UserTaskState])
  extends TaskActor[UserTaskState] {

  def start(state: UserTaskState): Unit = {
    // todo: Оповещение о новой задаче через веб-сокет
    //    for {
    //      //x1 <- notificationManager.pushWebSocketNotification(WebSocketNotification(p3.watcherIds, s"Form completed"))
    //      x2 <- processManager.userTaskOperationComplete(nodes.processId.get, nodes.operationId.get, getState.defStorageActor)
    //    } shutdown(getState)
  }

  def tryUpdateForm(state: State, event: CqrsEvent): Unit = {
    Try(state.updated(event)) match {
      case Success(updatedState) =>
        persist(event) { event =>
          changeState(updatedState)
          bpm.completeTask(id)
          sender() ! FormSubmit(Success(id))
        }
      case Failure(e) =>
        sender() ! FormSubmit(Failure(e))
    }
  }

  def submitForm(state: UserTaskState, data: Json): Unit = {
    if (state.submitted) sender() ! FormAlreadySubmitted else tryUpdateForm(state, SubmitFormEvt(data))
  }

  def behavior(state: UserTaskState) = {
    case SubmitFormCmd(x) => submitForm(state, x)
  }
}

case class UserTaskState(storage: RunnableStorage, submitted: Boolean = false) extends TaskState {
  def update: Update = {
    case SubmitFormEvt(x) =>
      val updatedStorage = storage.copy(defStorage = storage.defStorage.updateJson(x))
      copy(storage = updatedStorage, submitted = true)
  }
}

object UserTaskState {
  def empty = UserTaskState(RunnableStorage.empty)
}

object UserTaskActor {
  def props(taskId: Task.Id, bpm: Bpm, state: UserTaskState = UserTaskState.empty)(implicit c: ExecutionContext, t: Timeout): Props = {
    Props(new UserTaskActor(id = taskId, bpm = bpm, initState = state))
  }
}