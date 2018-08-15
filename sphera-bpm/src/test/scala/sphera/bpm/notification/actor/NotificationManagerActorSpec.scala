package sphera.bpm.notification.actor

import akka.actor.ActorSystem
import akka.actor.SupervisorStrategy._
import akka.testkit.{ TestActorRef, TestKit }
import sphera.bpm.notification.NewNotificationManager
import sphera.core.test.PersistenceSpec

class NotificationManagerActorSpec extends TestKit(ActorSystem("NotificationManagerActorSpec"))
  with PersistenceSpec
  with NewNotificationManager
  with NewMailNotificationActor
  with NewSmsNotificationActor
  with NewSmsVerificationActor {
  "A supervisor strategy of the NotificationManagerActor" when receive {
    "ArithmeticException" must {
      "resume actorRef " in {
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new ArithmeticException) shouldBe Resume
      }
    }
    "NullPointerException" must {
      "restart actorRef " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new NullPointerException) shouldBe Restart
      }
    }
    "IllegalArgumentException" must {
      "resume actorRef " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new IllegalArgumentException) shouldBe Resume
      }
    }
    "Exception" must {
      "restart actorRef " in {
        val p = newTestProbeRef
        val supervisor = TestActorRef[NotificationManagerActor](NotificationManagerActor.props(generateActorId, config))
        val strategy = supervisor.underlyingActor.supervisorStrategy.decider
        strategy(new Exception) shouldBe Resume
      }
    }
  }
  "A NotificationManagerActor" when receive {
    "MailNotificationManagerActor.Command" must {
      "forward it" in {
        for {
          a <- newNotificationManagerActor()
          x <- generateMailNotificationPassword()
          y <- ask(a, MailNotificationActor.CreateNotificationCmd(x))
        } yield y shouldBe MailNotificationActor.Done
      }
    }
    "SmsNotificationManagerActor.Command" must {
      "forward it" in {
        for {
          a <- newNotificationManagerActor()
          x <- generateSmsNotificationPassword()
          y <- ask(a, SmsNotificationActor.CreateNotificationCmd(x))
        } yield y shouldBe SmsNotificationActor.Done
      }
    }
    "SmsVerificationServiceActor.Command" must {
      "forward it" in {
        for {
          a <- newNotificationManagerActor()
          x <- generateSmsVerification()
          y <- ask(a, SmsVerificationActor.CreateVerificationCmd(x))
        } yield y shouldBe SmsVerificationActor.Done
      }
    }
  }
}