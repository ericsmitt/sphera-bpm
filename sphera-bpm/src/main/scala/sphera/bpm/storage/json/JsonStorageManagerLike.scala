package sphera.bpm.storage.json

import akka.actor.ActorRef
import sphera.bpm.JsonValueNotFoundException
import sphera.bpm.model.Modifier
import sphera.bpm.storage.NodeContainer
import sphera.bpm.storage.Storage.NodeUpdatedTry
import sphera.bpm.storage.json.JsonStorage._
import sphera.core.akkaext.actor._
import io.circe.Json

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait JsonStorageManagerLike extends NodeContainer {
  def getJson(path: Path): Future[Json] = getNode(path.nodeId).flatMap(getJson(_, path.dataPath))

  def getJson(node: ActorRef, path: DataPath): Future[Json] =
    ask(node, GetJson(path))
      .mapTo[JsonOpt]
      .map(_.x)
      .map {
        case Some(y) => y
        case None => throw JsonValueNotFoundException(path)
      }

  def updateJson(path: Path, json: Json)(implicit modifier: Modifier): Future[ActorId] = getNode(path.nodeId).flatMap(updateJson(_, path.dataPath, json))

  def updateJson(node: ActorRef, path: DataPath, json: Json)(implicit modifier: Modifier): Future[ActorId] =
    ask(node, UpdateJsonCmd(path, json, modifier))
      .mapTo[NodeUpdatedTry]
      .map(_.nodeIdTry)
      .map {
        case Success(y) => y
        case Failure(e) => throw e
      }

  def addJson(path: Path, json: Json)(implicit modifier: Modifier): Future[ActorId] = getNode(path.nodeId).flatMap(addJson(_, path.dataPath, json))

  def addJson(node: ActorRef, path: DataPath, json: Json)(implicit modifier: Modifier): Future[ActorId] =
    ask(node, AddJsonCmd(path, json, modifier))
      .mapTo[NodeUpdatedTry]
      .map(_.nodeIdTry)
      .map {
        case Success(y) => y
        case Failure(e) => throw e
      }

}

