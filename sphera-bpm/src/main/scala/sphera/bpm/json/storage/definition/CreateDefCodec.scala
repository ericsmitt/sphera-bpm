package sphera.bpm.json.storage.definition

import java.util.UUID

import sphera.bpm.Implicits._
import sphera.bpm.storage.definition._
import sphera.core.akkaext.actor.{ DataPath, Path }
import io.circe._
//import io.circe.generic.AutoDerivation
import io.circe.syntax._
//import io.circe.generic.auto._

trait CreateDefCodec extends DefCodec {
  implicit val encodeCreateStringDef: Encoder[CreateStringDef] = (x: CreateStringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.StringTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateIntDef: Encoder[CreateIntDef] = (x: CreateIntDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.IntTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateBooleanDef: Encoder[CreateBooleanDef] = (x: CreateBooleanDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.BooleanTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateDecimalDef: Encoder[CreateDecimalDef] = (x: CreateDecimalDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.DecimalTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateObjectDef: Encoder[CreateObjectDef] = (x: CreateObjectDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("mutable", x.mutable.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ObjectTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateMLStringDef: Encoder[CreateMLStringDef] = (x: CreateMLStringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.MLStringTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateArrayDef: Encoder[CreateArrayDef] = (x: CreateArrayDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ArrayTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateRootDef: Encoder[CreateRootDef] = (x: CreateRootDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("mutable", x.mutable.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RootTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateRoleDef: Encoder[CreateRoleDef] = (x: CreateRoleDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateRoleGroupDef: Encoder[CreateRoleGroupDef] = (x: CreateRoleGroupDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleGroupTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateRolesDef: Encoder[CreateRolesDef] = (x: CreateRolesDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RolesTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateRefDef: Encoder[CreateRefDef] = (x: CreateRefDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RefTypeName.asJson),
    ("patch", Patch.Create.value.asJson))

  implicit val encodeCreateDef: Encoder[CreateDef] = {
    case x: CreateStringDef => x.asJson
    case x: CreateIntDef => x.asJson
    case x: CreateBooleanDef => x.asJson
    case x: CreateDecimalDef => x.asJson
    case x: CreateObjectDef => x.asJson
    case x: CreateMLStringDef => x.asJson
    case x: CreateArrayDef => x.asJson
    case x: CreateRootDef => x.asJson
    case x: CreateRoleDef => x.asJson
    case x: CreateRoleGroupDef => x.asJson
    case x: CreateRolesDef => x.asJson
    case x: CreateRefDef => x.asJson
  }

  implicit val decodeCreateStringDef: Decoder[CreateStringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[String]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateStringDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeCreateIntDef: Decoder[CreateIntDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[Int]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateIntDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeCreateBooleanDef: Decoder[CreateBooleanDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[Boolean]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateBooleanDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeCreateDecimalDef: Decoder[CreateDecimalDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[BigDecimal]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateDecimalDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeCreateObjectDef: Decoder[CreateObjectDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fMutable <- c.downField("mutable").as[Boolean]
    fValue <- c.downField("value").as[Map[String, Def]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateObjectDef(name = fName, description = fDescription, mutable = fMutable, value = fValue, path = fPath)

  implicit val decodeCreateMLStringDef: Decoder[CreateMLStringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Map[String, StringDef]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateMLStringDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeCreateArrayDef: Decoder[CreateArrayDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[List[Def]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateArrayDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeCreateRootDef: Decoder[CreateRootDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fMutable <- c.downField("mutable").as[Boolean]
    fValue <- c.downField("value").as[Map[String, Def]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateRootDef(name = fName, description = fDescription, mutable = fMutable, value = fValue, path = fPath)

  implicit val decodeCreateRoleDef: Decoder[CreateRoleDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[UUID]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateRoleDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeCreateRoleGroupDef: Decoder[CreateRoleGroupDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Map[String, RoleDef]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateRoleGroupDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeCreateRolesDef: Decoder[CreateRolesDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Map[String, RoleGroupDef]]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateRolesDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeCreateRefDef: Decoder[CreateRefDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Path]
    fPath <- c.downField("path").as[Option[DataPath]]
  } yield CreateRefDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeCreateDef: Decoder[CreateDef] = (c: HCursor) => (for {
    fType <- c.downField("type").as[String]
  } yield fType).flatMap {
    case Def.StringTypeName => decodeCreateStringDef(c)
    case Def.IntTypeName => decodeCreateIntDef(c)
    case Def.BooleanTypeName => decodeCreateBooleanDef(c)
    case Def.DecimalTypeName => decodeCreateDecimalDef(c)
    case Def.ObjectTypeName => decodeCreateObjectDef(c)
    case Def.MLStringTypeName => decodeCreateMLStringDef(c)
    case Def.ArrayTypeName => decodeCreateArrayDef(c)
    case Def.RootTypeName => decodeCreateRootDef(c)
    case Def.RoleTypeName => decodeCreateRoleDef(c)
    case Def.RoleGroupTypeName => decodeCreateRoleGroupDef(c)
    case Def.RolesTypeName => decodeCreateRolesDef(c)
    case Def.RefTypeName => decodeCreateRefDef(c)
  }
}