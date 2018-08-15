package sphera.bpm.process

import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.runnable.RunnableManager._
import sphera.bpm.runnable.{ Runnable, RunnableManager, RunnableStatus }
import sphera.core.test.PersistenceSpec

import scala.concurrent.duration._

trait RunnableExpectations { _: PersistenceSpec with TestKit =>
  def expectCreateRunnable(p: TestProbe): Runnable.Id = {
    p.expectMsgType[CreatedRunnableInfoEvt].info.id
  }

  def expectUpdateRunnable(p: TestProbe): Runnable.Id = {
    p.expectMsgType[UpdatedRunnableInfoEvt].update.id
  }

  def expectStartRunnable(p: TestProbe): Runnable.Id = {
    expectCreateRunnable(p)
    expectUpdateRunnable(p)
  }

  def fishForStartRunnable(p: TestProbe): Runnable.Id = {
    val event = p.fishForSpecificMessage[UpdatedRunnableInfoEvt](1 second, s"StartRunnable") {
      case event: UpdatedRunnableInfoEvt if event.update.status.contains(RunnableStatus.Started) => event
    }
    event.update.id
  }

  implicit class SubscribedOnRunnableManagerEventsTestProbe(p: TestProbe) {
    def withSubscriptionOnRunnableEvents: TestProbe = {
      system.eventStream.subscribe(p.ref, classOf[Runnable.Event])
      system.eventStream.subscribe(p.ref, classOf[RunnableManager.Event])
      p
    }
  }
}
