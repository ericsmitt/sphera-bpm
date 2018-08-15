package sphera.bpm.notification.actor

import akka.actor.ActorRef
import akka.testkit.TestProbe
import sphera.bpm.notification.{ SmsVerification, Verification }
import sphera.core.akkaext.actor.ActorId
import sphera.core.test.PersistenceSpec
import org.scalatest.Assertion

import scala.concurrent.Future

trait NewSmsVerificationActor extends NewSmsNotificationActor { _: PersistenceSpec =>
  import SmsVerificationActor._

  def expectAddNotificationCmd(p: TestProbe, x: SmsVerification): Assertion =
    p.expectMsgType[SmsNotificationActor.CreateNotificationCmd].x shouldBe x.notification

  def createSmsVerification(a: ActorRef, x: SmsVerification): Future[Any] =
    ask(a, CreateVerificationCmd(x))

  def deleteSmsVerification(a: ActorRef, id: Verification.Id): Future[Any] =
    ask(a, DeleteVerificationCmd(id))

  def getSmsVerification(a: ActorRef): Future[SmsVerification] =
    ask(a, GetVerification).mapTo[VerificationOpt].map(_.x.get)

  def getSmsVerifications(a: ActorRef): Future[Map[Verification.Id, SmsVerification]] =
    ask(a, GetVerifications).mapTo[VerificationMap].map(_.x)

  def smsVerify(a: ActorRef, id: Verification.Id, code: String): Future[Any] =
    ask(a, VerifyCmd(id, code))

  def generateSmsVerification(id: Verification.Id = generateUUID): Future[SmsVerification] =
    for (x <- generateSmsNotificationVerification()) yield {
      SmsVerification(
        id = id,
        notification = x)
    }

  def newSmsVerificationActor(id: ActorId = generateActorId, state: SmsVerificationState = SmsVerificationState.empty): Future[(TestProbe, ActorRef)] = Future {
    val smsNotificationServiceActorProbe = newTestProbe
    val a = system.actorOf(SmsVerificationActor.props(id, smsNotificationServiceActorProbe.ref, state), id.name)
    (smsNotificationServiceActorProbe, a)
  }
}