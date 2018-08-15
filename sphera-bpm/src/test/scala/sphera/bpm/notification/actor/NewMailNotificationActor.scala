package sphera.bpm.notification.actor

import java.net.{ ConnectException, SocketException }

import akka.actor.ActorRef
import sphera.bpm.notification.client.MailClient
import sphera.bpm.notification.{ MailNotification, MailSettings, Notification, NotificationConfig }
import sphera.core.akkaext.actor.ActorId
import sphera.core.test.PersistenceSpec
import com.typesafe.config.ConfigFactory
import org.scalamock.scalatest.AsyncMockFactory

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait NewMailNotificationActor extends NotificationConfig with AsyncMockFactory { _: PersistenceSpec =>
  import MailNotificationActor._

  lazy val mailNotificationConfig: MailNotificationEntry =
    ConfigFactory.load().getConfig("sphera").mailNotificationEntry

  def createMailNotification(a: ActorRef, x: MailNotification): Future[Any] =
    ask(a, CreateNotificationCmd(x))

  def getMailNotifications(a: ActorRef): Future[Map[Notification.Id, MailNotification]] =
    ask(a, GetNotifications).mapTo[NotificationMap].map(_.x)

  def mailNotify(a: ActorRef): Future[Any] =
    ask(a, NotifyCmd)

  def generateMailNotificationPassword(id: Notification.Id = generateUUID): Future[MailNotification] = Future {
    MailNotification.Password(
      id = id,
      email = generateEmail,
      subject = generateString(),
      message = generateString(),
      password = generatePassword,
      retry = 2)
  }

  def generateMailNotificationTextMessage(id: Notification.Id = generateUUID): Future[MailNotification] = Future {
    MailNotification.TextMessage(
      id = id,
      email = generateEmail,
      subject = generateString(),
      message = generateString(),
      retry = 2)
  }

  def newMailNotificationActor(id: ActorId = generateActorId, state: MailNotificationState = MailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationActor.props(id, mailNotificationConfig.retryInterval, mailNotificationConfig.mail), id.name)
  }

  def stubbedMailClient(settings: MailSettings): MailClient = {
    val x = stub[MailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Success(MailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Success(MailClient.SendSuccess)
    }
    x
  }

  def stubbedSocketExceptionMailClient(settings: MailSettings): MailClient = {
    val x = stub[MailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Success(MailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Failure(new SocketException("Socket exception"))
    }
    x
  }

  def stubbedConnectionRefusedMailClient(settings: MailSettings): MailClient = {
    val x = stub[MailClient]
    x.settings _ when () returns settings
    x.connect _ when () returns Failure(new ConnectException("Connection refused"))
    x
  }

  def stubbedSocketExceptionMailClientInDebug(settings: MailSettings): MailClient = {
    val s = settings.copy(debug = true)
    val x = stub[MailClient]
    x.settings _ when () returns s
    x.connect _ when () returns Success(MailClient.Connected)
    x.send _ when (*, *, *) onCall { (to: String, subject: String, message: String) =>
      Failure(new SocketException("Socket exception"))
    }
    x
  }

  def newStubbedMailNotificationActor(id: ActorId = generateActorId, state: MailNotificationState = MailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      mailClient = stubbedMailClient(mailNotificationConfig.mail)), id.name)
  }

  def newStubbedSocketExceptionMailNotificationActor(id: ActorId = generateActorId, state: MailNotificationState = MailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      mailClient = stubbedSocketExceptionMailClient(mailNotificationConfig.mail)), id.name)
  }

  def newStubbedSocketExceptionMailNotificationActorInDebug(id: ActorId = generateActorId, state: MailNotificationState = MailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      mailClient = stubbedSocketExceptionMailClientInDebug(mailNotificationConfig.mail)), id.name)
  }

  def newStubbedConnectionRefusedMailNotificationActor(id: ActorId = generateActorId, state: MailNotificationState = MailNotificationState.empty): Future[ActorRef] = Future {
    system.actorOf(MailNotificationActor.propsWithMailClient(
      id = id,
      retryInterval = 1 hour,
      mailClient = stubbedConnectionRefusedMailClient(mailNotificationConfig.mail)), id.name)
  }
}
