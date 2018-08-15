package sphera.bpm.notification.actor

import sphera.core.akkaext.actor.CqrsState

case class WebSocketNotificationState() extends CqrsState {
  def update = PartialFunction.empty
}

object WebSocketNotificationState {
  def empty = WebSocketNotificationState()
}
