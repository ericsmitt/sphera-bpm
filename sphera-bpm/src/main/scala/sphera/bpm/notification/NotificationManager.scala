package sphera.bpm.notification

import akka.actor.ActorRef
import akka.util.Timeout
import sphera.core.akkaext.actor.ActorId

import scala.concurrent.ExecutionContext

class NotificationManager(val notificationManagerActor: ActorRef)(implicit val c: ExecutionContext, val t: Timeout)
  extends NotificationManagerLike

object NotificationManager {
  type Id = ActorId

  def apply(notificationManagerActor: ActorRef)(implicit c: ExecutionContext, t: Timeout): NotificationManager =
    new NotificationManager(notificationManagerActor)
}