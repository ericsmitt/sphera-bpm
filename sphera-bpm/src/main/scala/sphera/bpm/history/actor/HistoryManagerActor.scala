package sphera.bpm.history.actor

import akka.actor.Props
import akka.util.Timeout
import sphera.bpm.history.HistoryManager
import sphera.bpm.history.actor.HistoryManagerActor._
import sphera.bpm.history.model.HistoryData
import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import sphera.core.akkaext.persistence._

import scala.concurrent.ExecutionContext

class HistoryManagerActor(val id: HistoryManager.Id, val initState: HistoryManagerState)(implicit val executionContext: ExecutionContext, val timeout: Timeout)
  extends CqrsPersistentActor[HistoryManagerState] {

  def createHistoryData(state: HistoryManagerState, x: HistoryData): Unit = {
    if (state.historyDataExists(x.id)) sender() ! HistoryDataAlreadyExists
    else {
      persist(state, CreatedHistoryDataEvt(x)) { (state, event) =>
        publish(event)
        sender() ! Done
      }
    }
  }

  def findHistoryDataById(state: HistoryManagerState, historyDataId: HistoryData.Id): Unit =
    sender ! HistoryDataOpt(state.getHistoryDataOpt(historyDataId))

  def findHistoryDataAll(state: HistoryManagerState): Unit =
    sender() ! History(state.getHistoryDataAll)

  def behavior(state: HistoryManagerState): Receive = {
    case CreateHistoryDataCmd(x) => createHistoryData(state, x)
    case GetHistoryDataById(x) => findHistoryDataById(state, x)
    case GetHistoryDataAll => findHistoryDataAll(state)
  }
}

object HistoryManagerActor {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  case class CreateHistoryDataCmd(x: HistoryData) extends Command

  case class GetHistoryDataById(historyDataId: HistoryData.Id) extends Request
  case object GetHistoryDataAll extends Request

  case class CreatedHistoryDataEvt(x: HistoryData) extends Event

  case object Done extends Response
  case object HistoryDataAlreadyExists extends Response
  case class HistoryDataOpt(x: Option[HistoryData]) extends Response
  case class History(x: Map[HistoryData.RawId, HistoryData]) extends Response

  def props(id: HistoryManager.Id, state: HistoryManagerState = HistoryManagerState.empty)(implicit c: ExecutionContext, t: Timeout) = {
    Props(new HistoryManagerActor(id, state))
  }
}

