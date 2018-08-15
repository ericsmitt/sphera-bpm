package sphera.bpm

import java.util.UUID

import sphera.bpm.bpmn.BpmnId
import sphera.bpm.json._
import sphera.bpm.storage.definition.Patch.Compound
import sphera.bpm.storage.definition._
import sphera.bpm.storage.document.DocumentStorage
import sphera.bpm.storage.json.JsonStorage
import sphera.bpm.storage.schema.JsonSchema
import sphera.core.akkaext.actor._
import com.outworkers.phantom.dsl.UUID
import io.circe._

import scala.concurrent.duration.{ Duration, FiniteDuration }

object Implicits {
  implicit def uuidToString(x: UUID): String = x.toString
  implicit def stringToUUID(x: String): UUID = UUID.fromString(x)
  implicit def toBooleanDef(x: Boolean): BooleanDef = BooleanDef("", None, None, x, "not_set")
  implicit def toStringDef(x: String): StringDef = StringDef("", None, None, x, "not_set")
  implicit def toDecimalDef(x: BigDecimal): DecimalDef = DecimalDef("", None, None, x, "not_set")
  implicit def toIntDef(x: Int): IntDef = IntDef("", None, None, x, "not_set")
  implicit def toDecimalDef(x: Float): DecimalDef = DecimalDef("", None, None, BigDecimal(x), "not_set")
  implicit def toDecimalDef(x: Double): DecimalDef = DecimalDef("", None, None, BigDecimal(x), "not_set")

  implicit def toBpmnId(x: String): BpmnId = BpmnId(x)
  implicit def toBpmnIdList(x: Iterable[String]): List[BpmnId] = x.map(BpmnId.apply).toList
  implicit def toBpmnIdSet(x: Iterable[String]): Set[BpmnId] = x.map(BpmnId.apply).toSet

  implicit def toDataPathDefMap(x: Map[String, Def]): Map[DataPath, Def] = x.map({ case (p, d) => DataPath(p) -> d })
  implicit def jsonObjectToJson(x: JsonObject): Json = x.asJson
  implicit def jsonToJsonStorage(x: Json): JsonStorage = JsonStorage(x)
  implicit def jsonToString(x: Json): String = x.toString()
  implicit def toDefStorage(x: Def): DefStorage = DefStorage(x.asJson)
  implicit def toDefSchemaStorage(x: JsonSchema[Def]): DefSchemaStorage = DefSchemaStorage(x.asJson)

  implicit def defStorageToJson(x: DefStorage): Json = x.repr
  implicit def defStorageToString(x: DefStorage): String = x.toString
  implicit def defSchemaStorageToJson(x: DefSchemaStorage): Json = x.repr
  implicit def defSchemaStorageToString(x: DefSchemaStorage): String = x.toString

  implicit def toBpmException(x: Throwable): BpmException = x match {
    case e: BpmException => e
    case e => UnknownException(e.getMessage)
  }

  implicit def toBpmExceptionOpt(x: Throwable): Option[BpmException] = Some(toBpmException(x))

  implicit def toCompoundPatch(x: String): Compound = Compound(x)
  implicit def toPatch(x: String): Patch = Patch(x)
  implicit def toCompoundPatchOpt(x: Option[String]): Option[Compound] = x.map(toCompoundPatch)
  implicit def toPatchOpt(x: Option[String]): Option[Patch] = x.map(toPatch)

  implicit def fromCompoundPatch(x: Compound): String = x.toString
  implicit def fromPatch(x: Patch): String = x.toString

  //implicit def toOption[A, B](x: A)(implicit convert: A => B): Option[B] = Some(convert(x))
  //implicit def toList[T](d: Map[String, T]): List[T] = d.values.toList

  implicit def longToFiniteDuration(x: Long): FiniteDuration = Duration.fromNanos(x)
  implicit def finiteDurationToLong(x: FiniteDuration): Long = x.toNanos
}
