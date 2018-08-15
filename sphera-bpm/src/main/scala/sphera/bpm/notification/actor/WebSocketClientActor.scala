package sphera.bpm.notification.actor

import akka.actor.{ Actor, ActorRef, Props }
import sphera.bpm.notification.WebSocketNotification
import sphera.core.domain.tenancy.model.User

class WebSocketClientActor(userId: User.Id, notificationManagerActor: ActorRef) extends Actor {
  import WebSocketClientActor._
  import WebSocketNotificationActor._

  def waiting: Receive = {
    case Connected(x) =>
      context.become(connected(x))
      notificationManagerActor ! ConnectCmd(userId, self)
  }
  def connected(a: ActorRef): Receive = {
    case IncomingMessage(x) =>
    case x: WebSocketNotification => a ! OutgoingMessage(x.message)
  }
  def receive: Receive = waiting
}

object WebSocketClientActor {
  sealed trait UserMessage
  case class Connected(x: ActorRef)
  case class IncomingMessage(text: String) extends UserMessage
  case class OutgoingMessage(text: String) extends UserMessage

  def props(x: User.Id, y: ActorRef): Props = Props(new WebSocketClientActor(x, y))
}
