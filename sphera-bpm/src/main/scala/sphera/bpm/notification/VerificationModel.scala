package sphera.bpm.notification

import java.util.UUID

import scala.concurrent.duration.{ FiniteDuration, _ }

trait Verification {
  def id: Verification.Id
  def duration: FiniteDuration
}

object Verification {
  type Id = UUID
}

case class SmsVerification(
  id: Verification.Id,
  notification: SmsNotification.Verification,
  duration: FiniteDuration = 10.minute) extends Verification