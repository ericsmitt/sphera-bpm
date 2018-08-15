package sphera.bpm.storage.schema

import sphera.bpm.storage.definition._
import sphera.bpm.storage.schema.JsonSchema._
import io.circe._
import io.circe.syntax._

import scala.reflect.runtime.universe._

trait DefSchema {
  def mapToTypeName[T <: Def](implicit tag: WeakTypeTag[T]): String = tag.tpe match {
    case t if t =:= weakTypeOf[IntDef] => Def.IntTypeName
    case t if t =:= weakTypeOf[StringDef] => Def.StringTypeName
    case t if t =:= weakTypeOf[BooleanDef] => Def.BooleanTypeName
    case t if t =:= weakTypeOf[DecimalDef] => Def.DecimalTypeName
    case t if t =:= weakTypeOf[ObjectDef] => Def.ObjectTypeName
    case t if t =:= weakTypeOf[MLStringDef] => Def.MLStringTypeName
    case t if t =:= weakTypeOf[ArrayRef] => Def.ArrayTypeName
    case t if t =:= weakTypeOf[RootDef] => Def.RootTypeName
    case t if t =:= weakTypeOf[RolesDef] => Def.RolesTypeName
    case t if t =:= weakTypeOf[RoleGroupDef] => Def.RoleGroupTypeName
    case t if t =:= weakTypeOf[RoleDef] => Def.RoleTypeName
    case t if t =:= weakTypeOf[RefDef] => Def.RefTypeName
  }

  def mapToValueSchema(typeName: String, additionalProperties: Boolean): Json = typeName match {
    case Def.IntTypeName => intSchema.asJson
    case Def.StringTypeName => strSchema.asJson
    case Def.BooleanTypeName => boolSchema.asJson
    case Def.DecimalTypeName => doubleSchema.asJson
    case Def.RefTypeName => pathSchema.asJson
    case Def.MLStringTypeName => mapSchema[String, StringDef].asJson
    case Def.RoleTypeName => uuidSchema.asJson
    case Def.RoleGroupTypeName => mapSchema[String, RoleDef].asJson
    case Def.RolesTypeName => mapSchema[String, RoleGroupDef].asJson
    case Def.ArrayTypeName => objectSchema[ArrayDef](List.empty, additionalProperties).asJson
    case Def.ObjectTypeName => objectSchema[ObjectDef](List.empty, additionalProperties).asJson
    case Def.RootTypeName => objectSchema[RootDef](List.empty, additionalProperties).asJson
  }

  def buildDefSchema[T <: Def](additionalProperties: Boolean = false)(implicit tag: WeakTypeTag[T]): JsonSchema[T] = {
    val typeName = mapToTypeName
    val pName = "name" -> Json.obj("type" -> "string".asJson)
    val pDescription = "description" -> Json.obj("type" -> "string".asJson)
    val pValue = "value" -> mapToValueSchema(typeName, additionalProperties)
    val pPath = "path" -> Json.obj("type" -> "string".asJson)
    val pType = "type" -> Json.obj("const" -> typeName.asJson)
    val baseProperties = List(pName, pDescription, pValue, pPath, pType)
    val properties = typeName match {
      case Def.ObjectTypeName | Def.RootTypeName =>
        val pAdditionalProperties = "mutable" -> Json.obj("const" -> additionalProperties.asJson)
        baseProperties :+ pAdditionalProperties
      case Def.IntTypeName | Def.StringTypeName | Def.BooleanTypeName | Def.DecimalTypeName =>
        val pIndex = "index" -> Json.obj("type" -> "string".asJson)
        baseProperties :+ pIndex
      case x =>
        baseProperties
    }
    objectSchema[T](properties, additionalProperties)
  }

  implicit def objectSchema[T](properties: List[(String, Json)], additionalProperties: Boolean = false)(implicit tag: WeakTypeTag[T]): JsonSchema[T] = {
    val required = properties.map(_._1)
      .filterNot(_ == "description")
      .filterNot(_ == "index")

    inlineInstance[T](
      Map(
        "type" -> "object".asJson,
        "required" -> required.asJson,
        "additionalProperties" -> additionalProperties.asJson,
        "minProperties" -> required.size.asJson,
        "properties" -> Json.obj(properties: _*)).asJsonObject)
  }

  implicit val intDefSchema: JsonSchema[IntDef] = buildDefSchema[IntDef]()
  implicit val stringDefSchema: JsonSchema[StringDef] = buildDefSchema[StringDef]()
  implicit val booleanDefSchema: JsonSchema[BooleanDef] = buildDefSchema[BooleanDef]()
  implicit val decimalDefSchema: JsonSchema[DecimalDef] = buildDefSchema[DecimalDef]()
  implicit val mlStringDefSchema: JsonSchema[MLStringDef] = buildDefSchema[MLStringDef]()
  implicit val roleDefSchema: JsonSchema[RoleDef] = buildDefSchema[RoleDef]()
  implicit val roleGroupDefSchema: JsonSchema[RoleGroupDef] = buildDefSchema[RoleGroupDef]()
  implicit val rolesDefSchema: JsonSchema[RolesDef] = buildDefSchema[RolesDef]()
  implicit val refDefSchema: JsonSchema[RefDef] = buildDefSchema[RefDef]()

  def objectDefSchema(additionalProperties: Boolean): JsonSchema[ObjectDef] = buildDefSchema[ObjectDef](additionalProperties)
  def rootDefSchema(additionalProperties: Boolean): JsonSchema[RootDef] = buildDefSchema[RootDef](additionalProperties)

  val definitions: Set[Definition] = Set(
    intDefSchema.definition,
    stringDefSchema.definition,
    booleanDefSchema.definition,
    decimalDefSchema.definition,
    refDefSchema.definition,
    mlStringDefSchema.definition,
    roleDefSchema.definition,
    roleGroupDefSchema.definition,
    rolesDefSchema.definition)

  val definitionsAsJson = Json.obj("definitions" -> Json.fromFields(definitions.map(x => x.id -> x.json).toMap))
}