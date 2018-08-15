package sphera.bpm.storage.json

import sphera.bpm.Implicits._
import sphera.bpm.json._
import sphera.bpm.model.Modifier
import sphera.bpm.storage.Storage
import sphera.bpm.storage.schema.SchemaValidator.validate
import sphera.bpm.{ JsonValueNotFoundException, RequiredArrayOrObjectException }
import sphera.core.akkaext.actor.Path._
import sphera.core.akkaext.actor._
import com.outworkers.phantom.dsl.UUID
import io.circe.{ ACursor, Json, JsonObject }
import scala.annotation.tailrec

trait JsonStorageLike {
  type A <: JsonStorageLike
  type B <: JsonStorageSchemaLike

  def repr: Json
  def schema: Option[B]

  def withRepr(repr: Json): A
  def withSchema(schema: B): A

  def apply(path: DataPath): Option[Json] = {
    @tailrec
    def traverse(cursor: ACursor, fragments: List[String]): Option[Json] = {
      fragments match {
        case "*" :: Nil => cursor.focus.map(_.toJsonArray)
        case "*" :: xs => cursor.focus.map(_.filterByPath(xs))
        case ArrayIndexPattern(x) :: Nil => cursor.downN(x.toInt).focus
        case ArrayIndexPattern(x) :: xs => traverse(cursor.downN(x.toInt), xs)
        case x :: Nil => cursor.downField(x).focus
        case x :: xs => traverse(cursor.downField(x), xs)
        case Nil => Option(repr)
      }
    }
    val cursor = repr.hcursor
    traverse(cursor, path.fragments)
  }

  /**
   * Filter array or object elements tree by path.
   */
  def filterByPath(path: DataPath): Json = {
    repr.arrayOrObject(
      throw RequiredArrayOrObjectException(path),
      _.toList.map(_.apply(path)).asJson,
      _.toList.map({ case (k, v) => k -> v(path) }).asJson)
  }

  def toJsonArray: Json = {
    repr.arrayOrObject(
      Json.arr(),
      x => x.asJson,
      x => Json.fromValues(x.toVector.map(_.asJson)))
  }

  //def objectToVector: Vector[]

  def toVector: Vector[Json] = {
    repr.arrayOrObject(
      Vector.empty,
      identity,
      x => x.toVector.map(_.asJson))
  }

  def exists(path: DataPath): Boolean = apply(path).isDefined

  def getIntOpt(path: DataPath): Option[Int] = apply(path).get.asNumber.get.toInt

  def getStringOpt(path: DataPath): Option[String] = apply(path).get.asString

  def getDecimalOpt(path: DataPath): Option[BigDecimal] = apply(path).get.asNumber.get.toBigDecimal

  def getBooleanOpt(path: DataPath): Option[Boolean] = apply(path).get.asBoolean

  def getListOpt(path: DataPath): Option[List[Json]] = apply(path).flatMap(_.asArray.map(_.toList))

  def getJsonObjectOpt(path: DataPath): Option[Vector[(String, Json)]] = {
    @tailrec
    def traverse(cursor: ACursor, fragments: List[String]): Option[Vector[(String, Json)]] = {
      fragments match {
        case "*" :: Nil => cursor.focus.flatMap(_.asObject).map(_.toVector)
        case "*" :: xs =>
          cursor.focus.flatMap(_.asObject)
            .map(_.toVector)
            .map(_.flatMap({ case (k, v) => v(path).map(v => k -> v).toVector }))
        case x :: Nil => cursor.downField(x).focus.flatMap(_.asObject).map(_.toVector)
        case x :: xs => traverse(cursor.downField(x), xs)
        case Nil => repr.asObject.map(_.toVector)
      }
    }
    val cursor = repr.hcursor
    traverse(cursor, path.fragments)
  }

  def updateJson(that: Json): A = updateJson(RootDataPath, that)

  def updateJson(path: DataPath, that: Json): A = {
    @tailrec
    def traverse(cursor: ACursor, fragments: List[String]): A = fragments match {
      case ArrayIndexPattern(x) :: Nil => withRepr(cursor.downN(x.toInt).withFocus(_.deepMerge(that)).top.get)
      case ArrayIndexPattern(x) :: xs => traverse(cursor.downN(x.toInt), xs)
      case x :: Nil =>

        val xCursor = cursor.downField(x)
        xCursor.focus match {
          case Some(_) => withRepr(xCursor.withFocus(_.deepMerge(that)).top.get)
          case None =>
            println(path)
            println(cursor.focus)
            withRepr(cursor.withFocus(_.deepMerge(Json.obj(x -> that))).top.get)
        }

      case x :: xs => traverse(cursor.downField(x), xs)
      case Nil => withRepr(repr.deepMerge(that))
    }
    val cursor = repr.hcursor
    val tr = traverse(cursor, path.fragments)
    schema.map(_.repr).foreach(validate(tr.repr, _))
    tr
  }

  def updateInt(path: DataPath, value: Int): A = updateValue(path, value)

  def updateString(path: DataPath, value: String): A = updateValue(path, value)

  def updateBoolean(path: DataPath, value: Boolean): A = updateValue(path, value)

  def updateDecimal(path: DataPath, value: BigDecimal): A = updateValue(path, value)

  def updateUUID(path: DataPath, value: UUID): A = updateValue(path, value)

  def updatePath(path: DataPath, value: Path): A = updateValue(path, value)

  private def updateValue(path: DataPath, value: Any): A = {
    @tailrec
    def traverse(cursor: ACursor, fragments: List[String], jValue: Json): A = fragments match {
      case ArrayIndexPattern(x) :: Nil => withRepr(cursor.downN(x.toInt).withFocus(_ => jValue).top.get)
      case ArrayIndexPattern(x) :: xs => traverse(cursor.downN(x.toInt), xs, jValue)
      case x :: Nil => withRepr(cursor.downField(x).withFocus(_ => jValue).top.get)
      case x :: xs => traverse(cursor.downField(x), xs, jValue)
      case Nil => throw JsonValueNotFoundException(path)
    }
    val cursor = repr.hcursor
    val jValue = value match {
      case x: Int => Json.fromInt(x)
      case x: String => Json.fromString(x)
      case x: Boolean => Json.fromBoolean(x)
      case x: BigDecimal => Json.fromBigDecimal(x)
      case x: UUID => Json.fromString(x.toString)
      case x: Path => Json.fromString(x.raw)
    }
    val tr = traverse(cursor, path.fragments, jValue)
    schema.map(_.repr).foreach(validate(tr.repr, _))
    tr
  }

  /**
   * = Add json with schema validation =
   */
  def createJson(path: DataPath, that: Json): A = {
    val updated = addJson(path, that)
    schema.map(_.repr).foreach(validate(updated.repr, _))
    updated
  }

  /**
   * = Add json without schema validation =
   *
   * Use only for building json structure.
   */
  def addJson(that: Json): A = addJson(RootDataPath, that)

  def addJson(path: DataPath, that: Json): A = {
    @tailrec
    def traverse(json: Json, fr1: List[String], fr2: List[String]): A = fr1 match {
      case x :: Nil => traverse(Json.obj(fr2.head -> json), Nil, fr2.tail)
      case x :: xs => traverse(Json.obj(fr2.head -> json), xs, fr2.tail)
      case Nil => withRepr(repr.deepMerge(json))
    }
    traverse(that, path.fragments, path.fragments.reverse)
  }

  def deleteJson(path: DataPath) = {
    @tailrec
    def traverse(cursor: ACursor, fragments: List[String]): A = fragments match {
      case Nil => withRepr(Json.fromString(""))
      case ArrayIndexPattern(x) :: Nil => withRepr(cursor.downN(x.toInt).delete.top.get)
      case ArrayIndexPattern(x) :: xs => traverse(cursor.downN(x.toInt), xs)
      case x :: Nil => withRepr(cursor.downField(x).delete.top.get)
      case x :: xs => traverse(cursor.downField(x), xs)
    }
    val cursor = repr.hcursor
    traverse(cursor, path.fragments)
  }

  case class FindAllByKeyEntry(key: String, value: Json, parent: Json)

  /**
   * Recursively return all values matching the specified key.
   */
  def findAllByKey(key: String): List[FindAllByKeyEntry] = keyValues(key, repr, None).collect {
    case (k, v, Some(x)) if k == key => FindAllByKeyEntry(k, v, x)
    case (k, v, None) if k == key => FindAllByKeyEntry(k, v, Json.obj())
  }

  /**
   * All (key, value, parent json).
   */
  def keyValues(key: String, json: Json, parent: Option[Json], acc: List[(String, Json, Option[Json])] = List.empty): List[(String, Json, Option[Json])] = {
    def objectKeyValues(obj: JsonObject): List[(String, Json, Option[Json])] = {
      obj
        .toList
        .flatMap({ case (k, v) => keyValues(k, v, Some(obj.asJson)) })
    }

    json.arrayOrObject(
      acc :+ (key, json, parent),
      acc ++ _.flatMap(_.asObject).toList.flatMap(objectKeyValues),
      acc ++ objectKeyValues(_))
  }

  def ++(that: A): A = withRepr(this.repr.deepMerge(that.repr))

  override def toString = repr.toString()
}

trait JsonStorageLikeTyped[A1 <: JsonStorageLike, B1 <: JsonStorageSchemaLike] extends JsonStorageLike {
  type A = A1
  type B = B1
}

case class JsonStorage(repr: Json, schema: Option[JsonStorageSchema] = None) extends JsonStorageLikeTyped[JsonStorage, JsonStorageSchema] {
  def withRepr(repr: Json): JsonStorage = copy(repr = repr)
  def withSchema(schema: JsonStorageSchema): JsonStorage = copy(schema = Some(schema))
}

object JsonStorage {
  trait Command extends Storage.Command
  trait Request extends Storage.Request
  trait Response extends Storage.Response
  trait Event extends Storage.Event

  case class GetJson(path: DataPath) extends Request

  case class UpdateJsonCmd(path: DataPath, json: Json, modifier: Modifier) extends Command
  case class AddJsonCmd(path: DataPath, json: Json, modifier: Modifier) extends Command
  case class DeleteJsonCmd(path: DataPath, modifier: Modifier) extends Command

  case class UpdatedJsonEvt(path: DataPath, json: Json, modifier: Modifier) extends Event
  case class AddedJsonEvt(path: DataPath, json: Json, modifier: Modifier) extends Event
  case class DeletedJsonEvt(path: DataPath, modifier: Modifier) extends Event

  case class JsonOpt(x: Option[Json]) extends Response

  def empty: JsonStorage = JsonStorage(Json.obj(), None)
}
