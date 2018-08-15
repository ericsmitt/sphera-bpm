package sphera.bpm.notification

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.bpm.notification.actor.NewSmsVerificationActor
import sphera.core.test.PersistenceSpec

class NotificationManagerSpec extends TestKit(ActorSystem("NotificationManagerSpec"))
  with PersistenceSpec
  with NewNotificationManager
  with NewSmsVerificationActor {
  "A NotificationManagerSpec" when {
    "push" must {
      "verify" in {
        //        def push = {
        //          for {
        //            storage <- newNotificationManager()
        //            x1 <- generateSmsNotificationVerification()
        //          } yield {
        //            storage.push(x1)
        //            (storage, x1)
        //          }
        //        }
        //
        //        for {
        //          (storage, x1) <- push
        //          _ <- waitSome
        //          _ <- waitSome
        //          x3 <- storage.getSmsNotifications
        //          x4 <- storage.verify(x1.data, x1.code)
        //          x5 <- storage.getSmsVerifications
        //        } yield {
        //          sealed trait("====")
        //          println(x5)
        //          1000 to 9999 contains x1.code.toInt shouldBe true
        //          x3.map(_.data) should contain(x1.data)
        //          x5 shouldBe empty
        //        }
        succeed
      }
    }
  }
}