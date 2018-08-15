package sphera.bpm.storage.document

import sphera.bpm.{ Bpm, DocumentNotFoundException }
import sphera.bpm.json._
import sphera.bpm.model.Modifier
import sphera.bpm.storage.Node
import sphera.bpm.storage.Storage.NodeUpdatedTry
import sphera.bpm.storage.definition.DefStorage.{ DefOpt, UpdatedDefValueOnlyEvt }
import sphera.bpm.storage.definition._
import sphera.bpm.storage.json.JsonStorage
import sphera.core.akkaext.actor._
import sphera.core.domain.tenancy.model.User
import io.circe.Json

import scala.concurrent.Future
import sphera.bpm.storage.Node
import sphera.bpm.json._

case class DocumentStorage(
  repr: Json,
  indexes: DefIndexStorage,
  schema: Option[DefSchemaStorage] = None) extends DefStorageLike[DocumentStorage] {

  def withRepr(repr: Json): DocumentStorage = new DocumentStorage(
    repr = repr,
    schema = schema,
    indexes = indexes)

  def withIndexes(indexes: DefIndexStorage): DocumentStorage = new DocumentStorage(
    repr = repr,
    schema = schema,
    indexes = indexes)

  def withSchema(schema: DefSchemaStorage): DocumentStorage = new DocumentStorage(
    repr = repr,
    schema = Some(schema),
    indexes = indexes)

  def mapSchema(f: DefSchemaStorage => DefSchemaStorage): DocumentStorage = new DocumentStorage(
    repr = repr,
    schema = schema.map(f),
    indexes = indexes)
}

object DocumentStorage {
  trait Command extends DefStorage.Command
  trait Request extends DefStorage.Request
  trait Response extends DefStorage.Response
  trait Event extends DefStorage.Event

  def apply(defStorage: DefStorage): DocumentStorage = {
    new DocumentStorage(
      repr = defStorage.repr,
      indexes = defStorage.indexes,
      schema = defStorage.schema)
  }

  def apply(
    repr: Json,
    indexes: DefIndexStorage,
    schema: DefSchemaStorage): DocumentStorage = {
    new DocumentStorage(
      repr = repr,
      indexes = indexes,
      schema = Some(schema))
  }

  def emptyRootDef(
    id: Node.Id,
    mutable: Boolean = false,
    indexes: DefIndexStorage = DefIndexStorage.empty,
    schema: Option[DefSchemaStorage] = None): DocumentStorage = {
    new DocumentStorage(
      repr = RootDef.empty(mutable).asJson,
      indexes = indexes,
      schema = schema)
  }
}