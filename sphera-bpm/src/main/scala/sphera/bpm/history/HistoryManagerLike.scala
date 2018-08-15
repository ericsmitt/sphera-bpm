package sphera.bpm.history

import akka.actor.ActorRef
import sphera.bpm.EntityNotFoundException
import sphera.bpm.history.actor.HistoryManagerActor._
import sphera.bpm.history.model.HistoryData
import sphera.bpm.model.EntityType
import sphera.core.akkaext.actor._
import scala.concurrent.Future

trait HistoryManagerLike extends FutureSupport {
  def historyManagerActor: ActorRef

  private def valueOrNotFound[T <: Any](historyDataId: HistoryData.Id): PartialFunction[Option[T], T] = {
    case Some(x) => x
    case None => throw EntityNotFoundException(historyDataId.raw, EntityType.HistoryData)
  }

  def createHistoryData(x: HistoryData): Future[Response] =
    ask(historyManagerActor, CreateHistoryDataCmd(x))
      .mapTo[Response]

  def getHistoryData(historyDataId: HistoryData.Id): Future[HistoryData] =
    ask(historyManagerActor, GetHistoryDataById(historyDataId))
      .mapTo[HistoryDataOpt]
      .map(_.x)
      .map(valueOrNotFound(historyDataId))

  def getHistoryDataAll: Future[Map[HistoryData.RawId, HistoryData]] =
    ask(historyManagerActor, GetHistoryDataAll)
      .mapTo[History]
      .map(_.x)
}
