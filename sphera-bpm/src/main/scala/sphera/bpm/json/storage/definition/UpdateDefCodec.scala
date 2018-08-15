package sphera.bpm.json.storage.definition

import java.util.UUID

import sphera.bpm.json.{ decodeUpdateArrayDef, decodeUpdateObjectDef, decodeUpdateRefDef, decodeUpdateRoleDef, decodeUpdateRoleGroupDef, decodeUpdateRolesDef, decodeUpdateRootDef, _ }
import sphera.bpm.storage.definition.Patch.Compound
import sphera.bpm.storage.definition._
import sphera.core.akkaext.actor.{ DataPath, Path }
import io.circe.{ Decoder, Encoder, HCursor, Json }
import sphera.bpm.Implicits._

trait UpdateDefCodec {
  implicit val encodeUpdateStringDef: Encoder[UpdateStringDef] = (x: UpdateStringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.StringTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateIntDef: Encoder[UpdateIntDef] = (x: UpdateIntDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.IntTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateBooleanDef: Encoder[UpdateBooleanDef] = (x: UpdateBooleanDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.BooleanTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateDecimalDef: Encoder[UpdateDecimalDef] = (x: UpdateDecimalDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.DecimalTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateObjectDef: Encoder[UpdateObjectDef] = (x: UpdateObjectDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ObjectTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateMLStringDef: Encoder[UpdateMLStringDef] = (x: UpdateMLStringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.MLStringTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateArrayDef: Encoder[UpdateArrayDef] = (x: UpdateArrayDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ArrayTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateRootDef: Encoder[UpdateRootDef] = (x: UpdateRootDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RootTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateRoleDef: Encoder[UpdateRoleDef] = (x: UpdateRoleDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateRoleGroupDef: Encoder[UpdateRoleGroupDef] = (x: UpdateRoleGroupDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleGroupTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateRolesDef: Encoder[UpdateRolesDef] = (x: UpdateRolesDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RolesTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateRefDef: Encoder[UpdateRefDef] = (x: UpdateRefDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RefTypeName.asJson),
    ("patch", Patch.Update.value.asJson))

  implicit val encodeUpdateDef: Encoder[UpdateDef] = {
    case x: UpdateStringDef => x.asJson
    case x: UpdateIntDef => x.asJson
    case x: UpdateBooleanDef => x.asJson
    case x: UpdateDecimalDef => x.asJson
    case x: UpdateObjectDef => x.asJson
    case x: UpdateMLStringDef => x.asJson
    case x: UpdateArrayDef => x.asJson
    case x: UpdateRootDef => x.asJson
    case x: UpdateRoleDef => x.asJson
    case x: UpdateRoleGroupDef => x.asJson
    case x: UpdateRolesDef => x.asJson
    case x: UpdateRefDef => x.asJson
  }

  implicit val decodeUpdateStringDef: Decoder[UpdateStringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateStringDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeUpdateIntDef: Decoder[UpdateIntDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[Int]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateIntDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeUpdateBooleanDef: Decoder[UpdateBooleanDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[Boolean]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateBooleanDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeUpdateDecimalDef: Decoder[UpdateDecimalDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[BigDecimal]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateDecimalDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeUpdateMLStringDef: Decoder[UpdateMLStringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchStringDef]]]
    fPatch <- c.downField("patch").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateMLStringDef(name = fName, description = fDescription, value = fValue, patch = fPatch, path = fPath)

  implicit val decodeUpdateObjectDef: Decoder[UpdateObjectDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchDef]]]
    fPatch <- c.downField("patch").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateObjectDef(name = fName, description = fDescription, value = fValue, patch = fPatch, path = fPath)

  implicit val decodeUpdateArrayDef: Decoder[UpdateArrayDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[List[PatchDef]]]
    fPatch <- c.downField("patch").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateArrayDef(name = fName, description = fDescription, value = fValue, patch = fPatch, path = fPath)

  implicit val decodeUpdateRootDef: Decoder[UpdateRootDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchDef]]]
    fPatch <- c.downField("patch").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateRootDef(name = fName, description = fDescription, value = fValue, patch = fPatch, path = fPath)

  implicit val decodeUpdateRoleDef: Decoder[UpdateRoleDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[UUID]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateRoleDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeUpdateRoleGroupDef: Decoder[UpdateRoleGroupDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchRoleDef]]]
    fPatch <- c.downField("patch").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateRoleGroupDef(name = fName, description = fDescription, value = fValue, patch = fPatch, path = fPath)

  implicit val decodeUpdateRolesDef: Decoder[UpdateRolesDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchRoleGroupDef]]]
    fPatch <- c.downField("patch").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateRolesDef(name = fName, description = fDescription, value = fValue, patch = fPatch, path = fPath)

  implicit val decodeUpdateRefDef: Decoder[UpdateRefDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Path]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield UpdateRefDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeUpdateDef: Decoder[UpdateDef] = (c: HCursor) => {
    c.downField("type").asType.flatMap(mapToUpdateDef(_, c))
  }

  def mapToUpdateDef(defType: DefType, c: HCursor): Decoder.Result[UpdateDef] = defType.fold(
    decodeUpdateIntDef(c),
    decodeUpdateStringDef(c),
    decodeUpdateBooleanDef(c),
    decodeUpdateDecimalDef(c),
    decodeUpdateRefDef(c),
    decodeUpdateRoleDef(c),
    decodeUpdateRoleGroupDef(c),
    decodeUpdateRolesDef(c),
    decodeUpdateMLStringDef(c),
    decodeUpdateArrayDef(c),
    decodeUpdateObjectDef(c),
    decodeUpdateRootDef(c))
}
