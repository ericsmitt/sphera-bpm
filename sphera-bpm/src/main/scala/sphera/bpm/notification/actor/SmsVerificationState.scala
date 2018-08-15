package sphera.bpm.notification.actor

import sphera.bpm.notification.actor.SmsVerificationActor._
import sphera.bpm.notification.{ SmsVerification, Verification }
import sphera.core.akkaext.actor.CqrsState
import sphera.core.utils.Generator

case class SmsVerificationState(v: Map[Verification.Id, SmsVerification]) extends CqrsState with Generator {
  def nonEmpty: Boolean = v.nonEmpty
  def create(x: SmsVerification): SmsVerificationState = copy(v + (x.id -> x))
  def delete(id: Verification.Id): SmsVerificationState = copy(v - id)
  def exists(id: Verification.Id): Boolean = v.get(id).isDefined
  def getById(id: Verification.Id): Option[SmsVerification] = v.get(id)
  def getAll: Map[Verification.Id, SmsVerification] = v.mapValues { x =>
    val code = hide(x.notification.code)
    x.copy(notification = x.notification.copy(code = code))
  }
  def getIds: Seq[Verification.Id] = v.keys.toSeq
  def update = {
    case CreatedVerificationEvt(x) => create(x)
    case DeletedVerificationEvt(x) => delete(x)
  }
}

object SmsVerificationState {
  def empty = SmsVerificationState(Map.empty)
}