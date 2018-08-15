package sphera.bpm.notification.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.notification.client.SmsClient
import sphera.bpm.notification.{ Notification, SmsNotification, SmsSettings }
import sphera.core.akkaext.actor._
import sphera.core.akkaext.persistence._
import sphera.core.utils.Generator

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{ FiniteDuration, _ }
import scala.util.{ Failure, Success, Try }

private class SmsNotificationActor(
  val id: ActorId,
  val retryInterval: FiniteDuration,
  val smsClient: SmsClient,
  val initState: SmsNotificationState,
  implicit val t: Timeout = 30 seconds)(implicit val executor: ExecutionContext) extends CqrsPersistentActor[SmsNotificationState]
  with Generator {
  import SmsNotificationActor._

  type ClientResult = (SmsNotification, Try[SmsClient.Response])
  type ClientSuccess = (SmsNotification, SmsClient.Response)
  type ClientFailure = (SmsNotification, Throwable)

  def send(x: SmsNotification): ClientResult =
    x -> smsClient.send(
      id = x.id,
      to = x.phone,
      message = x.message,
      from = x.subject)

  def processResults(state: SmsNotificationState, x: Seq[ClientResult]): Unit =
    changeState((state /: x) {
      case (s, (n, Success(_))) => s.updated(DeletedNotificationEvt(n.id))
      case (s, (n, Failure(_))) => s.updated(UpdatedRetryEvt(n.id, n.retry - 1))
    })

  def hideCredentials: PartialFunction[(SmsNotification, Any), (SmsNotification, Any)] = {
    case (n: SmsNotification.Verification, x) if !smsClient.settings.debug =>
      (n.copy(code = hide(n.code)), x)
    case (n: SmsNotification.Password, x) if !smsClient.settings.debug =>
      (n.copy(password = hide(n.password)), x)
    case x => x
  }

  def processFailures(x: Seq[ClientFailure]): Unit = {
    val httpUnauthorizedException: Option[SmsClient.HttpUnauthorizedException] = x.map(_._2).collectFirst {
      case x: SmsClient.HttpUnauthorizedException => x
    }
    val otherExceptions: Seq[ClientFailure] = x.filter {
      case (n, x: SmsClient.HttpUnauthorizedException) => false
      case _ => true
    }
    httpUnauthorizedException.foreach { e =>
      log.error(s"Http Unauthorized Exception [$e]")
    }
    otherExceptions
      .map(hideCredentials)
      .foreach {
        case (n: SmsNotification.Password, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}, password: ${n.password}] [$e]")
        case (n, e) => log.warning(s"Failed ${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}] [$e]")
      }
  }

  def processSuccesses(success: Seq[ClientSuccess]): Unit =
    success
      .map(hideCredentials)
      .foreach {
        case (n: SmsNotification.Password, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}, password: ${n.password}]")
        case (n: SmsNotification.Verification, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}, code:${n.code}]")
        case (n, _) => log.info(s"${n.getClass.getSimpleName} [${n.id}, phone:${n.phone}]")
      }

  def notify(state: SmsNotificationState): Unit = {
    if (state.nonEmpty) {
      val notifications = state.v.values.toSeq

      val results: Seq[ClientResult] = notifications.map(send)

      val success: Seq[ClientSuccess] = results.collect {
        case (n, Success(r: SmsClient.Response)) => (n, r)
      }

      val failures: Seq[ClientFailure] = results.collect {
        case (n, Failure(e: SmsClient.Exception)) => (n, e)
      }

      processResults(state, results)
      processFailures(failures)
      processSuccesses(success)
    }
    notifyAfterRetry()
    replyDone()
  }

  def createNotification(state: SmsNotificationState, x: SmsNotification): Unit = {
    if (state.exists(x.id)) sender ! NotificationAlreadyExists else {
      changeState(state.updated(CreatedNotificationEvt(x)))
      replyDone()
    }
  }

  def findNotifications(state: SmsNotificationState): Unit =
    sender() ! NotificationMap(state.v)

  def behavior(state: SmsNotificationState): Receive = {
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

object SmsNotificationActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case object NotifyCmd extends CqrsRequest
  case class CreateNotificationCmd(x: SmsNotification) extends Command

  case object GetNotifications extends Request

  case class CreatedNotificationEvt(x: SmsNotification) extends Event
  case class DeletedNotificationEvt(notificationId: Notification.Id) extends Event
  case class UpdatedRetryEvt(notificationId: Notification.Id, retry: Int) extends Event

  case object Done extends Response
  case object NotifyTimeoutException extends Response
  case object NotificationAlreadyExists extends Response
  case object NotificationNotFound extends Response
  case class NotificationMap(x: Map[Notification.Id, SmsNotification]) extends Response

  def props(
    id: ActorId,
    retryInterval: FiniteDuration,
    settings: SmsSettings,
    state: SmsNotificationState = SmsNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new SmsNotificationActor(id, retryInterval, new SmsClient(settings), state))

  def propsWithSmsClient(
    id: ActorId,
    retryInterval: FiniteDuration,
    smsClient: SmsClient,
    state: SmsNotificationState = SmsNotificationState.empty)(implicit c: ExecutionContext): Props =
    Props(new SmsNotificationActor(id, retryInterval, smsClient, state))
}
