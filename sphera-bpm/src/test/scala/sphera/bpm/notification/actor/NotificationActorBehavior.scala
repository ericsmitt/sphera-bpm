package sphera.bpm.notification.actor

import akka.actor.ActorRef
import sphera.bpm.notification.{ MailNotification, Notification, SmsNotification }
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future

trait NotificationActorBehavior extends NewSmsNotificationActor
  with NewMailNotificationActor { _: PersistenceSpec =>
  def smsNotificationActor(): Unit = {
    createNotificationBehavior(newSmsNotificationActor(), generateSmsNotificationPassword())
    createNotificationBehavior(newSmsNotificationActor(), generateSmsNotificationVerification())
    createNotificationBehavior(newSmsNotificationActor(), generateSmsNotificationTextMessage())

    notifyBehavior(
      newActor = newStubbedSmsNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationActor(),
      generate = generateSmsNotificationPassword())

    notifyBehavior(
      newActor = newStubbedSmsNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationActor(),
      generate = generateSmsNotificationVerification())

    notifyBehavior(
      newStubbedSmsNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationActor(),
      generate = generateSmsNotificationTextMessage())

    /**
     * In debug mode
     */
    notifyBehavior(
      newActor = newStubbedSmsNotificationActorInDebug(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationManagerInDebug(),
      generate = generateSmsNotificationPassword())

    notifyBehavior(
      newActor = newStubbedSmsNotificationActorInDebug(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationManagerInDebug(),
      generate = generateSmsNotificationVerification())

    notifyBehavior(
      newStubbedSmsNotificationActorInDebug(),
      newActorWithFail = newStubbedConnectionRefusedSmsNotificationManagerInDebug(),
      generate = generateSmsNotificationTextMessage())
  }

  def mailNotificationActor(): Unit = {
    createNotificationBehavior(newMailNotificationActor(), generateMailNotificationPassword())
    createNotificationBehavior(newMailNotificationActor(), generateMailNotificationTextMessage())

    notifyBehavior(
      newActor = newStubbedMailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionMailNotificationActor(),
      generate = generateMailNotificationPassword())

    notifyBehavior(
      newActor = newStubbedMailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionMailNotificationActor(),
      generate = generateMailNotificationTextMessage())

    notifyBehavior(
      newActor = newStubbedMailNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedMailNotificationActor(),
      generate = generateMailNotificationPassword())

    notifyBehavior(
      newActor = newStubbedMailNotificationActor(),
      newActorWithFail = newStubbedConnectionRefusedMailNotificationActor(),
      generate = generateMailNotificationTextMessage())

    /**
     * In debug mode
     */
    notifyBehavior(
      newActor = newStubbedMailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionMailNotificationActorInDebug(),
      generate = generateMailNotificationPassword())

    notifyBehavior(
      newActor = newStubbedMailNotificationActor(),
      newActorWithFail = newStubbedSocketExceptionMailNotificationActorInDebug(),
      generate = generateMailNotificationTextMessage())
  }

  def createNotification(a: ActorRef, x: Notification): Future[Any] = x match {
    case y: SmsNotification => createSmsNotification(a, y)
    case y: MailNotification => createMailNotification(a, y)
  }

  def addNotificationN(a: ActorRef, x: Seq[Notification]): Future[Seq[Any]] =
    Future.sequence(x.map(createNotification(a, _)))

  def generateN(generate: => Future[Notification], n: Int): Future[Seq[Notification]] =
    Future.sequence((1 to n).map(_ => generate)).mapTo[Seq[Notification]]

  def getNotifications(a: ActorRef, x: Notification): Future[Map[Notification.Id, Notification]] = x match {
    case y: SmsNotification => getSmsNotifications(a)
    case y: MailNotification => getMailNotifications(a)
  }

  def notify(a: ActorRef, x: Notification): Future[Any] =
    x match {
      case y: SmsNotification => smsNotify(a)
      case y: MailNotification => mailNotify(a)
    }

  /**
   * Повторяющиеся тесты для различных типов уведомлений
   */
  def createNotificationBehavior(newActor: => Future[ActorRef], generate: => Future[Notification]): Unit = {
    s"[$generateTestName] create notification" in {
      for {
        a <- newActor
        x <- generate
        _ <- createNotification(a, x)
        y <- getNotifications(a, x)
      } yield {
        y should have size 1
        y should contain key x.id
      }
    }
  }

  def notifyBehavior(newActor: => Future[ActorRef], newActorWithFail: => Future[ActorRef], generate: => Future[Notification]): Unit = {
    val name: String = generateTestName

    s"[$name] notify recipients" in {
      for {
        a <- newActor
        n1 <- generate
        n2 <- generate
        _ <- createNotification(a, n1)
        _ <- createNotification(a, n2)
        x <- getNotifications(a, n1)
        _ <- notify(a, n1)
        y <- getNotifications(a, n1)
      } yield {
        x should not be empty
        y shouldBe empty
      }
    }
    s"[$name] update retry" in {
      for {
        a <- newActorWithFail
        n1 <- generate
        n2 <- generate
        _ <- createNotification(a, n1)
        _ <- createNotification(a, n2)
        x <- getNotifications(a, n1)
        _ <- notify(a, n1)
        y <- getNotifications(a, n1)
      } yield {
        y should not be empty
        y.values.map(_.retry) should contain only 1
      }
    }
    s"[$name] delete notifications" in {
      for {
        a <- newActorWithFail
        n <- generate
        _ <- createNotification(a, n)
        x <- getNotifications(a, n)
        _ <- notify(a, n)
        _ <- notify(a, n)
        y <- getNotifications(a, n)
      } yield y shouldBe empty
    }
  }
}
