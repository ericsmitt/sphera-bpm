package sphera.bpm.notification.actor

import akka.actor.SupervisorStrategy.{ Escalate, Restart, Resume }
import akka.actor.{ Actor, ActorLogging, ActorRef, OneForOneStrategy, Props }
import akka.util.Timeout
import sphera.bpm.notification._
import sphera.core.akkaext.actor.ActorLifecycleHooks
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class NotificationManagerActor(
  val id: NotificationManager.Id,
  val mailRetryInterval: FiniteDuration,
  val mailSettings: MailSettings,
  val smsRetryInterval: FiniteDuration,
  val smsSettings: SmsSettings)(implicit c: ExecutionContext, t: Timeout) extends Actor
  with ActorLogging
  with ActorLifecycleHooks {
  import NotificationManagerActor._

  val mailNotificationActor: ActorRef = {
    context.actorOf(
      props = MailNotificationActor.props(
        id = id / MailNotificationActorName,
        retryInterval = mailRetryInterval,
        settings = mailSettings),
      name = MailNotificationActorName)
  }

  val smsNotificationActor: ActorRef = {
    context.actorOf(
      props = SmsNotificationActor.props(
        id = id / SmsNotificationActorName,
        retryInterval = smsRetryInterval,
        settings = smsSettings),
      name = SmsNotificationActorName)
  }

  val smsVerificationActor: ActorRef = {
    context.actorOf(
      props = SmsVerificationActor.props(
        id = id / SmsVerificationActorName,
        smsNotificationServiceActor = smsNotificationActor),
      name = SmsVerificationActorName)
  }

  val webSocketNotificationActor: ActorRef = {
    context.actorOf(
      props = WebSocketNotificationActor.props(
        id = id / WebSocketNotificationActorName),
      name = WebSocketNotificationActorName)
  }

  override val supervisorStrategy: OneForOneStrategy = {
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case e: ArithmeticException =>
        log.error(e, e.getMessage)
        Resume
      case e: NullPointerException =>
        log.error(e, e.getMessage)
        Restart
      case e: IllegalArgumentException =>
        log.debug(e.getMessage)
        Resume
      case e: Exception =>
        log.error(e, e.getMessage)
        Resume
      case e =>
        super.supervisorStrategy.decider.applyOrElse(e, (_: Any) => Escalate)
    }
  }

  def receive: Receive = {
    case x: MailNotificationActor.Command => mailNotificationActor forward x
    case x: SmsNotificationActor.Command => smsNotificationActor forward x
    case x: SmsVerificationActor.Command => smsVerificationActor forward x
    case x: WebSocketNotificationActor.Command => webSocketNotificationActor forward x

    case x: MailNotificationActor.Request => mailNotificationActor forward x
    case x: SmsNotificationActor.Request => smsNotificationActor forward x
    case x: SmsVerificationActor.Request => smsVerificationActor forward x
    case x: WebSocketNotificationActor.Request => webSocketNotificationActor forward x
  }
}

object NotificationManagerActor extends NotificationConfig {
  val MailNotificationActorName = "mail"
  val SmsNotificationActorName = "sms"
  val SmsVerificationActorName = "smsVerification"
  val WebSocketNotificationActorName = "ws"

  def props(
    id: NotificationManager.Id,
    config: Config)(implicit c: ExecutionContext, t: Timeout): Props = {
    val spheraConfig: Config = config.getConfig("sphera")
    val mailNotificationConfig = spheraConfig.mailNotificationEntry
    val smsNotificationConfig = spheraConfig.smsNotificationEntry
    Props(new NotificationManagerActor(
      id = id,
      mailRetryInterval = mailNotificationConfig.retryInterval,
      mailSettings = mailNotificationConfig.mail,
      smsRetryInterval = smsNotificationConfig.retryInterval,
      smsSettings = smsNotificationConfig.sms))
  }
}