package sphera.bpm.storage.definition

import sphera.bpm.json._
import sphera.bpm.storage.json.JsonStorageSchemaLikeTyped
import sphera.bpm.storage.schema.JsonSchema
import sphera.bpm.storage.schema.JsonSchema._
import sphera.core.akkaext.actor._
import io.circe.Json
import io.circe.optics.JsonPath._

import scala.reflect.runtime.universe._

case class DefSchemaStorage(repr: Json) extends JsonStorageSchemaLikeTyped[DefSchemaStorage] {
  def addRequiredProperty(property: String): DefSchemaStorage = {
    val _x = root.properties.value.required.arr.modify(_ :+ property.asJson)
    copy(repr = _x(repr))
  }

  def incMinProperties: DefSchemaStorage = {
    val _x = root.properties.value.minProperties.int.modify(_ + 1)
    copy(repr = _x(repr))
  }

  def addDefSchema(path: DataPath, json: Json): DefSchemaStorage = {
    addJson(resolveDefSchemaPath(path), json)
      .addRequiredProperty(path.name)
      .incMinProperties
  }

  def addDefSchema[T](path: DataPath, schema: JsonSchema[T])(implicit tag: WeakTypeTag[T]): DefSchemaStorage = {
    val x = tag.tpe match {
      case t if t =:= weakTypeOf[IntDef] => schema.asJsonRef
      case t if t =:= weakTypeOf[StringDef] => schema.asJsonRef
      case t if t =:= weakTypeOf[BooleanDef] => schema.asJsonRef
      case t if t =:= weakTypeOf[DecimalDef] => schema.asJsonRef
      case t if t =:= weakTypeOf[MLStringDef] => schema.asJsonRef
      case t if t =:= weakTypeOf[RolesDef] => schema.asJsonRef
      case t if t =:= weakTypeOf[RefDef] => schema.asJsonRef
    }
    addJson(resolveDefSchemaPath(path), x.asJson)
      .addRequiredProperty(path.name)
      .incMinProperties
  }

  def fragments(path: DataPath): List[String] = {
    path.fragments
      .zip(List.fill(path.fragments.size)(List("properties", "value", "properties")))
      .flatMap { case (x1, x2) => x2 :+ x1 }
  }

  def resolveDefSchemaPath(path: DataPath): DataPath = DataPath(fragments(path))
  def resolveSchemaValuePath(path: DataPath): DataPath = DataPath(fragments(path) ++ List("properties", "value"))

  def withRepr(repr: Json): DefSchemaStorage = copy(repr = repr)
  def withoutRoot: Json = root.properties.value.json.getOption(repr).get
  def withSchema(schema: DefSchemaStorage): DefSchemaStorage = this
  def map(f: Json => Json): DefSchemaStorage = copy(repr = f(repr))
}

object DefSchemaStorage {
  def emptyRootDef(additionalProperties: Boolean): DefSchemaStorage = {
    val schema = rootDefSchema(additionalProperties = additionalProperties).asJson
    DefSchemaStorage(schema).addJson(definitionsAsJson)
  }

  def emptyObjectDef(additionalProperties: Boolean): DefSchemaStorage = {
    val schema = objectDefSchema(additionalProperties = additionalProperties).asJson
    DefSchemaStorage(schema)
  }

  def emptyMLStringDef: DefSchemaStorage = DefSchemaStorage(mlStringDefSchema.asJson)
}
