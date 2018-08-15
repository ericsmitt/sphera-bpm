package sphera.bpm.json.storage.definition

import java.util.UUID

import sphera.bpm.storage.definition._
import sphera.core.akkaext.actor.{ DataPath, Path }
import io.circe.{ Decoder, Encoder, HCursor, Json }
import sphera.bpm.Implicits._
import sphera.bpm.json._

trait DeleteDefCodec {
  implicit val encodeDeleteStringDef: Encoder[DeleteStringDef] = (x: DeleteStringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.StringTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteIntDef: Encoder[DeleteIntDef] = (x: DeleteIntDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.IntTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteBooleanDef: Encoder[DeleteBooleanDef] = (x: DeleteBooleanDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.BooleanTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteDecimalDef: Encoder[DeleteDecimalDef] = (x: DeleteDecimalDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.DecimalTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteObjectDef: Encoder[DeleteObjectDef] = (x: DeleteObjectDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ObjectTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteMLStringDef: Encoder[DeleteMLStringDef] = (x: DeleteMLStringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.MLStringTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteArrayDef: Encoder[DeleteArrayDef] = (x: DeleteArrayDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ArrayTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteRootDef: Encoder[DeleteRootDef] = (x: DeleteRootDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RootTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteRoleDef: Encoder[DeleteRoleDef] = (x: DeleteRoleDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteRoleGroupDef: Encoder[DeleteRoleGroupDef] = (x: DeleteRoleGroupDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleGroupTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteRolesDef: Encoder[DeleteRolesDef] = (x: DeleteRolesDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RolesTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteRefDef: Encoder[DeleteRefDef] = (x: DeleteRefDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RefTypeName.asJson),
    ("patch", Patch.Delete.value.asJson))

  implicit val encodeDeleteDef: Encoder[DeleteDef] = {
    case x: DeleteStringDef => x.asJson
    case x: DeleteIntDef => x.asJson
    case x: DeleteBooleanDef => x.asJson
    case x: DeleteDecimalDef => x.asJson
    case x: DeleteObjectDef => x.asJson
    case x: DeleteMLStringDef => x.asJson
    case x: DeleteArrayDef => x.asJson
    case x: DeleteRootDef => x.asJson
    case x: DeleteRoleDef => x.asJson
    case x: DeleteRoleGroupDef => x.asJson
    case x: DeleteRolesDef => x.asJson
    case x: DeleteRefDef => x.asJson
  }

  implicit val decodeDeleteStringDef: Decoder[DeleteStringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[String]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteStringDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeDeleteIntDef: Decoder[DeleteIntDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[Int]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteIntDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeDeleteBooleanDef: Decoder[DeleteBooleanDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[Boolean]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteBooleanDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeDeleteDecimalDef: Decoder[DeleteDecimalDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fIndex <- c.downField("index").as[Option[Option[DataPath]]]
    fValue <- c.downField("value").as[Option[BigDecimal]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteDecimalDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeDeleteObjectDef: Decoder[DeleteObjectDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchDef]]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteObjectDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteMLStringDef: Decoder[DeleteMLStringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchStringDef]]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteMLStringDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteArrayDef: Decoder[DeleteArrayDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[List[PatchDef]]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteArrayDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteRootDef: Decoder[DeleteRootDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchDef]]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteRootDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteRoleDef: Decoder[DeleteRoleDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[UUID]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteRoleDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteRoleGroupDef: Decoder[DeleteRoleGroupDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchRoleDef]]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteRoleGroupDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteRolesDef: Decoder[DeleteRolesDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Map[String, PatchRoleGroupDef]]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteRolesDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteRefDef: Decoder[DeleteRefDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Option[Name]]
    fDescription <- c.downField("description").as[Option[Description]]
    fValue <- c.downField("value").as[Option[Path]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield DeleteRefDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDeleteDef: Decoder[DeleteDef] = (c: HCursor) => (for {
    fType <- c.downField("type").as[String]
  } yield fType).flatMap {
    case Def.StringTypeName => decodeDeleteStringDef(c)
    case Def.IntTypeName => decodeDeleteIntDef(c)
    case Def.BooleanTypeName => decodeDeleteBooleanDef(c)
    case Def.DecimalTypeName => decodeDeleteDecimalDef(c)
    case Def.ObjectTypeName => decodeDeleteObjectDef(c)
    case Def.MLStringTypeName => decodeDeleteMLStringDef(c)
    case Def.ArrayTypeName => decodeDeleteArrayDef(c)
    case Def.RootTypeName => decodeDeleteRootDef(c)
    case Def.RoleTypeName => decodeDeleteRoleDef(c)
    case Def.RoleGroupTypeName => decodeDeleteRoleGroupDef(c)
    case Def.RolesTypeName => decodeDeleteRolesDef(c)
    case Def.RefTypeName => decodeDeleteRefDef(c)
  }
}
