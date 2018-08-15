package sphera.bpm.notification.actor

import sphera.bpm.notification.actor.MailNotificationActor._
import sphera.bpm.notification.{ MailNotification, Notification }
import sphera.core.akkaext.actor.CqrsState

case class MailNotificationState(v: Map[Notification.Id, MailNotification]) extends CqrsState {
  def nonEmpty: Boolean = v.nonEmpty
  def add(x: MailNotification): MailNotificationState = copy(v + (x.id -> x))
  def delete(id: Notification.Id): MailNotificationState = copy(v - id)
  def exists(id: Notification.Id): Boolean = v.get(id).isDefined
  def getById(id: Notification.Id): Option[MailNotification] = v.get(id)
  def copyWithRetry(x: MailNotification, r: Int): MailNotification = x match {
    case x: MailNotification.Password => x.copy(retry = r)
    case x: MailNotification.TextMessage => x.copy(retry = r)
  }
  def updateRetry(id: Notification.Id, r: Int): MailNotificationState = {
    val x = v(id)
    if (r <= 0) delete(id) else copy(v + (x.id -> copyWithRetry(x, r)))
  }
  def update = {
    case CreatedNotificationEvt(x) => add(x)
    case DeletedNotificationEvt(x) => delete(x)
    case UpdatedRetryEvt(x, y) => updateRetry(x, y)
  }
}

object MailNotificationState {
  def empty = MailNotificationState(Map.empty)
}
