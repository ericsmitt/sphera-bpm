package sphera.bpm.notification.actor

import akka.actor.{ ActorRef, Props }
import akka.util.Timeout
import sphera.bpm.notification.actor.SmsVerificationActor._
import sphera.bpm.notification.{ SmsVerification, Verification }
import sphera.core.akkaext.actor._
import sphera.core.akkaext.persistence._

import scala.concurrent.ExecutionContext

private class SmsVerificationActor(
  val id: ActorId,
  val smsNotificationActor: ActorRef,
  val initState: SmsVerificationState)(implicit val executor: ExecutionContext, val timeout: Timeout) extends CqrsPersistentActor[SmsVerificationState] {

  def createVerification(state: SmsVerificationState, x: SmsVerification): Unit = {
    if (state.exists(x.id)) sender ! VerificationAlreadyExists else {
      persist(CreatedVerificationEvt(x)) { event =>
        changeState(state.updated(event))
        smsNotificationActor ! SmsNotificationActor.CreateNotificationCmd(x.notification)
        context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(x.id))
        sender ! Done
      }
    }
  }

  def deleteVerification(state: SmsVerificationState, id: Verification.Id): Unit = {
    if (state.exists(id)) {
      persist(DeletedVerificationEvt(id)) { event =>
        changeState(state.updated(event))
        sender ! Done
      }
    } else sender ! VerificationNotFound
  }

  def findVerification(state: SmsVerificationState, id: Verification.Id): Unit =
    sender() ! VerificationOpt(state.getById(id))

  def findVerifications(state: SmsVerificationState): Unit =
    sender() ! VerificationMap(state.getAll)

  def verify(state: SmsVerificationState, id: Verification.Id, code: String): Unit = {
    state.getById(id).fold(sender ! VerificationNotFound) { x =>
      if (x.notification.code == code) deleteVerification(state, id)
      else sender() ! InvalidCode
    }
  }

  def behavior(state: SmsVerificationState): Receive = {
    case CreateVerificationCmd(x) => createVerification(state, x)
    case DeleteVerificationCmd(x) => deleteVerification(state, x)
    case GetVerification(x) => findVerification(state, x)
    case GetVerifications => findVerifications(state)
    case VerifyCmd(x, y) => verify(state, x, y)
  }

  override def afterRecover(state: SmsVerificationState): Unit =
    state.getAll.values.foreach { x =>
      context.system.scheduler.scheduleOnce(x.duration, self, DeleteVerificationCmd(x.id))
    }
}

object SmsVerificationActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateVerificationCmd(x: SmsVerification) extends Command
  case class DeleteVerificationCmd(verificationId: Verification.Id) extends Command
  case class VerifyCmd(verificationId: Verification.Id, code: String) extends Command

  case class GetVerification(verificationId: Verification.Id) extends Request
  case object GetVerifications extends Request

  case class CreatedVerificationEvt(x: SmsVerification) extends Event
  case class DeletedVerificationEvt(verificationId: Verification.Id) extends Event

  case object Done extends Response
  case object VerificationAlreadyExists extends Response
  case object VerificationNotFound extends Response
  case class VerificationOpt(x: Option[SmsVerification]) extends Response
  case class VerificationMap(x: Map[Verification.Id, SmsVerification]) extends Response
  case object InvalidCode extends Response

  def props(
    id: ActorId,
    smsNotificationServiceActor: ActorRef,
    state: SmsVerificationState = SmsVerificationState.empty)(implicit c: ExecutionContext, t: Timeout): Props =
    Props(new SmsVerificationActor(id, smsNotificationServiceActor, state))
}

