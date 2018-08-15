package sphera.bpm.task.actor

import java.util.UUID

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.Bpm
import sphera.bpm.notification.MailNotification
import sphera.bpm.storage.runnable.RunnableStorage
import sphera.bpm.task.Task

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class SendTaskActor(
  val id: Task.Id,
  val bpm: Bpm,
  val initState: SendTaskState)(implicit val c: ExecutionContext, val t: Timeout, val tag: ClassTag[SendTaskState])
  extends TaskActor[SendTaskState] {

  def start(state: SendTaskState): Unit = {
    val recipients = state.defStorage.getDefStringListValue("recipients")
    val subject = state.defStorage.getDefStringValue("subject")
    val message = state.defStorage.getDefStringValue("message")

    sendMessage(recipients, subject, message) foreach { _ => bpm.completeTask(id) }
  }

  def sendMessage(recipients: List[String], subject: String, message: String) = {
    bpm.notificationManager
      .send(MailNotification.TextMessage(
        id = UUID.randomUUID(),
        email = recipients.mkString(","),
        subject = subject,
        message = message))
  }

  def recover(state: SendTaskState): Unit = {

  }

  def behavior(state: State): Receive = PartialFunction.empty
}

case class SendTaskState(storage: RunnableStorage) extends TaskState {
  def update: Update = PartialFunction.empty
}

object SendTaskState {
  def empty = SendTaskState(RunnableStorage.empty)
}

object SendTaskActor {
  def props(taskId: Task.Id, bpm: Bpm, state: SendTaskState = SendTaskState.empty)(implicit c: ExecutionContext, t: Timeout): Props = {
    Props(new SendTaskActor(id = taskId, bpm = bpm, initState = state))
  }
}