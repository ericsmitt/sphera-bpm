package sphera.bpm.notification

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.actor.NewMasterDataManager
import sphera.bpm.notification.actor.NotificationManagerActor
import sphera.core.akkaext.actor.ActorId
import sphera.core.test.PersistenceSpec
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.Future

trait NewNotificationManager extends NewMasterDataManager { _: PersistenceSpec with TestKit =>
  lazy val config: Config = ConfigFactory.load()

  def generateNotificationManagerId = ActorId(s"notificationManager-$generateInt")

  def newNotificationManagerActor(id: NotificationManager.Id = generateNotificationManagerId): Future[ActorRef] = Future {
    system.actorOf(
      props = NotificationManagerActor.props(
        id = id,
        config = config),
      name = id.name)
  }

  def newNotificationManager(id: NotificationManager.Id = generateNotificationManagerId): Future[NotificationManager] =
    newNotificationManagerActor()
      .map(NotificationManager(_))
}
