package sphera.bpm.notification.actor

import sphera.bpm.notification.actor.SmsNotificationActor._
import sphera.bpm.notification.{ Notification, SmsNotification }
import sphera.core.akkaext.actor.CqrsState

case class SmsNotificationState(v: Map[Notification.Id, SmsNotification]) extends CqrsState {
  def nonEmpty: Boolean = v.nonEmpty
  def add(x: SmsNotification): SmsNotificationState = copy(v + (x.id -> x))
  def delete(id: Notification.Id): SmsNotificationState = copy(v - id)
  def exists(id: Notification.Id): Boolean = v.get(id).isDefined
  def getById(id: Notification.Id): Option[SmsNotification] = v.get(id)
  def ids: Seq[Notification.Id] = v.keys.toSeq
  def copyWithRetry(x: SmsNotification, r: Int): SmsNotification = x match {
    case x: SmsNotification.Password => x.copy(retry = r)
    case x: SmsNotification.Verification => x.copy(retry = r)
    case x: SmsNotification.TextMessage => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): SmsNotificationState = {
    val x = v(id)
    if (r <= 0) delete(id) else copy(v + (x.id -> copyWithRetry(x, r)))
  }
  def update = {
    case CreatedNotificationEvt(x) => add(x)
    case DeletedNotificationEvt(x) => delete(x)
    case UpdatedRetryEvt(x, y) => updateRetry(x, y)
  }
}

object SmsNotificationState {
  def empty = SmsNotificationState(Map.empty)
}
