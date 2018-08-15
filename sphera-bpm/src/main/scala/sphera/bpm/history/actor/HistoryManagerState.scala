package sphera.bpm.history.actor

import sphera.bpm.history.actor.HistoryManagerActor._
import sphera.bpm.history.model.HistoryData
import sphera.core.akkaext.actor.CqrsState

case class HistoryManagerState(v: Map[HistoryData.RawId, HistoryData]) extends CqrsState {
  def createHistoryData(x: HistoryData): HistoryManagerState = copy(v + (x.id.raw -> x))
  def historyDataExists(x: HistoryData.Id): Boolean = v.get(x.raw).isDefined
  def getHistoryDataOpt(historyDataId: HistoryData.Id): Option[HistoryData] = v.get(historyDataId.raw)
  def getHistoryDataAll: Map[HistoryData.RawId, HistoryData] = v
  def getHistoryDataIds: Set[HistoryData.RawId] = v.keys.toSet

  def update: Update = {
    case CreatedHistoryDataEvt(x) => createHistoryData(x)
  }
}

object HistoryManagerState {
  def empty = HistoryManagerState(Map.empty)
}