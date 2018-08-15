package sphera.bpm.notification.actor

import akka.actor.Props
import sphera.bpm.notification.actor.MailNotificationActor._
import sphera.bpm.notification.client.MailClient
import sphera.bpm.notification.{ MailNotification, MailSettings, Notification }
import sphera.core.akkaext.actor._
import sphera.core.utils.Generator
import javax.mail.AuthenticationFailedException

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{ Failure, Success, Try }

private class MailNotificationActor(
  val id: ActorId,
  val retryInterval: FiniteDuration,
  val mailClient: MailClient,
  val initState: MailNotificationState)(implicit val executor: ExecutionContext)
  extends CqrsActor[MailNotificationState] with Generator {

  type ClientResult = (MailNotification, Try[MailClient.Response])
  type ClientSuccess = (MailNotification, MailClient.Response)
  type ClientFailure = (MailNotification, Throwable)

  def send(x: MailNotification): ClientResult =
    x -> mailClient.send(
      to = x.email,
      subject = x.subject,
      message = x.message)

  def processResults(state: State, x: Seq[ClientResult]): Unit = {
    changeState((state /: x) {
      case (s, (n, Success(_))) => s.updated(DeletedNotificationEvt(n.id)).asInstanceOf[State]
      case (s, (n, Failure(_))) => s.updated(UpdatedRetryEvt(n.id, n.retry - 1)).asInstanceOf[State]
    })
  }

  def hideCredentials: PartialFunction[(MailNotification, Any), (MailNotification, Any)] = {
    case (n: MailNotification.Password, x) if !mailClient.settings.debug =>
      (n.copy(password = hide(n.password)), x)
    case x => x
  }

  def processFailures(failures: Seq[ClientFailure]): Unit = {
    failures
      .map(hideCredentials)
      .foreach {
        case (n: MailNotification.Password, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, email:${n.email}, password: ${n.password}] [$e]")
        case (n, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, email:${n.email}] [$e]")
      }
  }

  def processSuccesses(success: Seq[ClientSuccess]): Unit = {
    success
      .map(hideCredentials)
      .foreach {
        case (n: MailNotification.Password, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, email:${n.email}, password: ${n.password}]")
        case (n, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, email:${n.email}]")
      }
  }

  def notify(state: State): Unit = {
    if (state.nonEmpty) {
      val notifications = state.v.values.toSeq
      mailClient.connect() match {
        case Success(_) =>
          val results: Seq[ClientResult] = notifications.map(send)

          val success: Seq[ClientSuccess] = results.collect {
            case (n, Success(r: MailClient.Response)) => (n, r)
          }

          val failures: Seq[ClientFailure] = results.collect {
            case (n, Failure(e: MailClient.Exception)) => (n, e)
          }

          processResults(state, results)
          processFailures(failures)
          processSuccesses(success)
          mailClient.disconnect()
        case Failure(e) =>
          changeState((state /: notifications) {
            case (s, n) => s.updated(UpdatedRetryEvt(n.id, n.retry - 1))
          })
          e match {
            case x: AuthenticationFailedException => log.error(s"Failed credentials [$e]")
            case _ => log.error(s"Failed creating new smtp connection [$e]")
          }
      }
    }
    notifyAfterRetry()
    replyDone()
  }

  def createNotification(state: MailNotificationState, x: MailNotification): Unit = {
    if (state.exists(x.id)) sender ! NotificationAlreadyExists else {
      changeState(state.updated(CreatedNotificationEvt(x)))
      replyDone()
    }
  }

  def findNotifications(state: MailNotificationState): Unit =
    sender() ! NotificationMap(state.v)

  def behavior(state: State): Receive = {
    case NotifyCmd => notify(state)
    case CreateNotificationCmd(x) => createNotification(state, x)
    case GetNotifications => findNotifications(state)
  }

  def notifyAfterRetry(): Unit =
    context.system.scheduler.scheduleOnce(
      delay = retryInterval,
      receiver = self,
      message = NotifyCmd)

  def replyDone(): Unit = if (sender != self) sender ! Done

  override def preStart(): Unit = {
    super.preStart()
    context.system.scheduler.scheduleOnce(
      delay = retryInterval,
      receiver = self,
      message = NotifyCmd)

  }
}

object MailNotificationActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case object NotifyCmd extends Command
  case class CreateNotificationCmd(x: MailNotification) extends Command

  case object GetNotifications extends CqrsRequest

  case class CreatedNotificationEvt(x: MailNotification) extends Event
  case class DeletedNotificationEvt(notificationId: Notification.Id) extends Event
  case class UpdatedRetryEvt(notificationId: Notification.Id, retry: Int) extends Event

  case object Done extends CqrsResponse
  case object NotificationAlreadyExists extends CqrsResponse
  case object NotificationNotFound extends CqrsResponse
  case class NotificationMap(x: Map[Notification.Id, MailNotification]) extends CqrsResponse

  def props(
    id: ActorId,
    retryInterval: FiniteDuration,
    settings: MailSettings,
    state: MailNotificationState = MailNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new MailNotificationActor(id, retryInterval, new MailClient(settings), state))

  def propsWithMailClient(
    id: ActorId,
    retryInterval: FiniteDuration,
    mailClient: MailClient,
    state: MailNotificationState = MailNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new MailNotificationActor(id, retryInterval, mailClient, state))
}