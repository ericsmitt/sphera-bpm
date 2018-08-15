package sphera.bpm.notification.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.core.test.PersistenceSpec

class MailNotificationActorSpec extends TestKit(ActorSystem("MailNotificationActorSpec"))
  with PersistenceSpec
  with NotificationActorBehavior {
  behave like mailNotificationActor()
}