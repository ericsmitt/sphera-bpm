package sphera.bpm.notification.actor

import akka.actor.ActorSystem
import akka.testkit.TestKit
import sphera.core.test.PersistenceSpec

class SmsNotificationActorSpec extends TestKit(ActorSystem("SmsNotificationActorSpec"))
  with PersistenceSpec
  with NotificationActorBehavior {
  behave like smsNotificationActor()
}