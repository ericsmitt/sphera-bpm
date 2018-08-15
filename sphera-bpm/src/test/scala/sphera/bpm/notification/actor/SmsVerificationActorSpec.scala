package sphera.bpm.notification.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.core.test.PersistenceSpec

class SmsVerificationActorSpec extends TestKit(ActorSystem("SmsVerificationActorSpec"))
  with PersistenceSpec
  with NewSmsVerificationActor {
  import SmsVerificationActor._

  "A SmsVerificationActor" when receive {
    "CreateVerificationCmd" must {
      "create verification SmsVerification" in {
        for {
          (_, a) <- newSmsVerificationActor()
          x <- generateSmsVerification()
          y <- createSmsVerification(a, x)
          z <- getSmsVerifications(a)
        } yield {
          y shouldBe Done
          z should have size 1
          z should contain key x.id
        }
      }
      "send CreateNotificationCmd" in {
        for {
          (p, a) <- newSmsVerificationActor()
          x <- generateSmsVerification()
          _ <- createSmsVerification(a, x)
          _ <- getSmsVerifications(a)
        } yield expectAddNotificationCmd(p, x)
      }
    }
    "DeleteNotificationCmd" must {
      "delete notification" in {
        for {
          (_, a) <- newSmsVerificationActor()
          v1 <- generateSmsVerification()
          v2 <- generateSmsVerification()
          x <- createSmsVerification(a, v1)
          y <- createSmsVerification(a, v2)
          z <- deleteSmsVerification(a, v1.id)
          q <- getSmsVerifications(a)
        } yield {
          x shouldBe Done
          y shouldBe Done
          q should have size 1
          q should contain key v2.id
        }
      }
    }
    "VerifyCmd" must {
      "verify and delete verification" in {
        for {
          (_, a) <- newSmsVerificationActor()
          v1 <- generateSmsVerification()
          v2 <- generateSmsVerification()
          x <- createSmsVerification(a, v1)
          y <- createSmsVerification(a, v2)
          z <- smsVerify(a, v1.id, v1.notification.code)
          q <- getSmsVerifications(a)
        } yield {
          x shouldBe Done
          y shouldBe Done
          z shouldBe Done
          q.keys should contain only v2.id
        }
      }
      "send ChangeStatusCmd" in {
        for {
          (_, a) <- newSmsVerificationActor()
          v <- generateSmsVerification()
          x <- createSmsVerification(a, v)
          y <- smsVerify(a, v.id, v.notification.code)
        } yield {
          x shouldBe Done
          y shouldBe Done
        }
      }
      "return InvalidCode" in {
        for {
          (_, a) <- newSmsVerificationActor()
          v <- generateSmsVerification()
          x <- createSmsVerification(a, v)
          y <- smsVerify(a, v.id, generateString())
        } yield {
          x shouldBe Done
          y shouldBe InvalidCode
        }
      }
    }
    "recover" must {
      "restore all verifications" in {
        val id = generateActorId
        for {
          (_, a) <- newSmsVerificationActor(id)
          v1 <- generateSmsVerification()
          v2 <- generateSmsVerification()
          _ <- createSmsVerification(a, v1)
          _ <- createSmsVerification(a, v2)
          _ <- kill(a)
          (_, a) <- newSmsVerificationActor(id)
          x <- getSmsVerifications(a)
        } yield x should have size 2
      }
      "restore all verifications after verify" in {
        val id = generateActorId
        for {
          (_, a) <- newSmsVerificationActor(id)
          v1 <- generateSmsVerification()
          v2 <- generateSmsVerification()
          _ <- createSmsVerification(a, v1)
          _ <- createSmsVerification(a, v2)
          _ <- smsVerify(a, v1.id, v1.notification.code)
          x <- getSmsVerifications(a)
          _ <- kill(a)
          (_, a) <- newSmsVerificationActor(id)
          y <- getSmsVerifications(a)
        } yield {
          x should have size 1
          y should have size 1
        }
      }
    }
  }
}