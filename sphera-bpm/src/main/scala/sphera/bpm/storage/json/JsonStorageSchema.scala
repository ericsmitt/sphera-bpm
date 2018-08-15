package sphera.bpm.storage.json

import sphera.core.akkaext.actor.{ CqrsCommand, CqrsEvent, CqrsRequest, CqrsResponse }
import io.circe.Json

trait JsonStorageSchemaLike extends JsonStorageLike {
  type A <: JsonStorageSchemaLike
  type B = A
  val schema = None
}

trait JsonStorageSchemaLikeTyped[Schema <: JsonStorageSchemaLikeTyped[Schema]] extends JsonStorageSchemaLike {
  type A = Schema
}

case class JsonStorageSchema(repr: Json) extends JsonStorageSchemaLikeTyped[JsonStorageSchema] {
  def withRepr(repr: Json): JsonStorageSchema = copy(repr = repr)
  def withSchema(schema: JsonStorageSchema): JsonStorageSchema = this
}

object JsonStorageSchema {
  trait Command extends CqrsCommand
  trait Request extends CqrsRequest
  trait Response extends CqrsResponse
  trait Event extends CqrsEvent

  def empty: JsonStorageSchema = JsonStorageSchema(Json.obj())
}
