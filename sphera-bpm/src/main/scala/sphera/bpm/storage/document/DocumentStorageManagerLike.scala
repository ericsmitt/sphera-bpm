package sphera.bpm.storage.document

import java.time.LocalTime

import akka.actor.ActorRef
import sphera.bpm.Implicits._
import sphera.bpm.model.Modifier
import sphera.bpm.storage.Storage.NodeUpdatedTry
import sphera.bpm.storage.definition.TraversableDefStorage
import sphera.bpm.storage.json.JsonStorageManagerLike
import sphera.core.akkaext.actor._

import scala.concurrent.Future

trait DocumentStorageManagerLike extends JsonStorageManagerLike with TraversableDefStorage {
  def nodeContainerActor: ActorRef

  def getDocument(path: Path): Future[Document] = getDocumentOpt(path).map(_.get)

  def getDocumentOpt(path: Path): Future[Option[Document]] = {
    val startGenTime = LocalTime.now().toNanoOfDay

    retrieveDefValueOpt(path).map {
      _.map { json =>
        val endGenTime = LocalTime.now()

        Document(
          body = json,
          path = path,
          genTime = endGenTime.minusNanos(startGenTime).toNanoOfDay)
      }
    }
  }

  def updateDocument(path: Path, update: UpdateDocument)(implicit modifier: Modifier): Future[List[NodeUpdatedTry]] = {
    writeDefValue(path, update.body)
  }
}

