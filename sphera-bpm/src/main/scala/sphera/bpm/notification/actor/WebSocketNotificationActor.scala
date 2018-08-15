package sphera.bpm.notification.actor

import akka.actor.{ ActorRef, Props, Terminated }
import sphera.bpm.notification.actor.WebSocketNotificationActor._
import sphera.bpm.notification.{ NotificationManager, WebSocketNotification }
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._
import sphera.core.domain.tenancy.model.User
private class WebSocketNotificationActor(val id: NotificationManager.Id, val initState: WebSocketNotificationState)
  extends CqrsPersistentActor[WebSocketNotificationState] {

  def connect(x: User.Id, y: ActorRef): Unit = {
    addSubscriber(self, x, y)
    context.watch(y)
  }
  def disconnect(x: ActorRef): Unit = removeSubscriber(self, x)

  def notify(x: WebSocketNotification): Unit = {
    subscribersMap
      .filter(x.userIds contains _._1)
      .foreach(_._2 ! x)
    sender() ! Done
  }

  def behavior(state: State): Receive = {
    case ConnectCmd(x, y) => connect(x, y)
    case NotifyCmd(x) => notify(x)
    case Terminated(x) => disconnect(x)
  }
}

object WebSocketNotificationActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class ConnectCmd(x: User.Id, y: ActorRef) extends CqrsRequest
  case class NotifyCmd(x: WebSocketNotification) extends CqrsRequest

  case object Done extends CqrsResponse

  def props(
    id: NotificationManager.Id,
    state: WebSocketNotificationState = WebSocketNotificationState.empty): Props =
    Props(new WebSocketNotificationActor(id, state))
}
