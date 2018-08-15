package sphera.bpm.storage.definition

import akka.actor.ActorRef
import sphera.bpm.model.Modifier
import sphera.bpm.storage.Node
import sphera.bpm.storage.Storage.NodeUpdatedTry
import sphera.bpm.storage.definition.DefStorage._
import sphera.bpm.storage.json.JsonStorageManagerLike
import sphera.bpm.{ DefNotFoundException, DefValueNotFoundException, RoleNotFoundException }
import sphera.core.akkaext.actor._
import sphera.core.domain.tenancy.model.User
import io.circe.Json

import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait DefStorageManagerLike extends JsonStorageManagerLike with TraversableDefStorage

