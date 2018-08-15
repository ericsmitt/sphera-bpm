package sphera.bpm.process

import akka.testkit.{ TestKit, TestProbe }
import sphera.bpm.history.actor.HistoryManagerActor
import sphera.bpm.history.actor.HistoryManagerActor.CreatedHistoryDataEvt
import sphera.bpm.history.model.HistoryData
import sphera.bpm.runnable.Runnable
import sphera.bpm.storage.definition.DefStorage
import sphera.core.test.PersistenceSpec

import scala.concurrent.duration._

trait HistoryExpectations { _: PersistenceSpec with TestKit =>
  def fishForCreateHistory(p: TestProbe): HistoryData = {
    val event = p.fishForSpecificMessage[CreatedHistoryDataEvt](2 second, "fishForCreateHistory") {
      case event @ CreatedHistoryDataEvt(x) => event
    }
    val historyId = event.x.id
    info(s"History created [${historyId.name}]")
    event.x
  }

  def fishForCreateHistory(p: TestProbe, runnableId: Runnable.Id): HistoryData = {
    val event = p.fishForSpecificMessage[CreatedHistoryDataEvt](2 second, s"fishForCreateHistory $runnableId") {
      case event @ CreatedHistoryDataEvt(x) if event.x.id == runnableId => event
    }
    info(s"History created [${runnableId.name}]")
    event.x
  }

  def fishForCreateHistoryGetDefStorage(p: TestProbe): DefStorage = {
    fishForCreateHistory(p).extendedInfo.state.defStorage
  }

  def fishForCreateHistoryGetDefStorage(p: TestProbe, runnableId: Runnable.Id): DefStorage = {
    fishForCreateHistory(p, runnableId: Runnable.Id).extendedInfo.state.defStorage
  }

  implicit class SubscribedOnHistoryEventsTestProbe(p: TestProbe) {
    def withSubscriptionOnHistoryEvents: TestProbe = {
      system.eventStream.subscribe(p.ref, classOf[HistoryManagerActor.Event])
      p
    }
  }
}
