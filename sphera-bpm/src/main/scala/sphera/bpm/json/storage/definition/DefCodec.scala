package sphera.bpm.json.storage.definition

import java.util.UUID

import sphera.bpm.storage.definition._
import sphera.core.akkaext.actor._
import io.circe._
//import io.circe.generic.AutoDerivation
import io.circe.syntax._
//import io.circe.generic.auto._

trait DefCodec {
  //  implicit val encodeMapStringAny: Encoder[Map[String, Any]] = (exception: Map[String, Any]) => Json.obj(
  //    exception.mapValues(_.asJson).toSeq: _*)
  //
  //  implicit val encodeAny: Encoder[Any] = {
  //    case x: String => x.asJson
  //    case x: Int => x.asJson
  //    case x: Boolean => x.asJson
  //    case x: BigDecimal => x.asJson
  //    case x: BigDecimal => x.asJson
  //    case x: Map[String, Def] => x.asJson
  //    case x: List[Def] => x.asJson
  //  }
  //
  //  implicit val decodeAny: Decoder[Any] = (c: HCursor) => {
  //    val value = c.value
  //    val booleanOpt = value.asBoolean
  //    val numberOpt = value.asNumber.flatMap { exception =>
  //      val intOpt = exception.toInt
  //      val biggerDecimalOpt = exception.toBigDecimal
  //      Seq(intOpt, biggerDecimalOpt).flatten.headOption
  //    }
  //    val stringOpt = value.asString
  //    val decodedOpt = Seq(booleanOpt, numberOpt, stringOpt).flatten.headOption
  //    decodedOpt match {
  //      case Some(exception) => Right(exception)
  //      case _ => Left(DecodingFailure("Any", c.history))
  //    }
  //  }

  //  implicit val encodeDefs: Encoder[Defs] =
  //    (exception: Defs) => Json.obj(("defStorage", exception.repr.asJson))
  //
  //  implicit val decodeDefs: Decoder[Defs] =
  //    (c: HCursor) => for (repr <- c.downField("defStorage").as[JsonRepr]) yield Defs(repr)
  //
  implicit val encodeDefStorage: Encoder[DefStorage] = (x: DefStorage) => x.repr

  implicit val decodeDefStorage: Decoder[DefStorage] = Decoder.decodeJson.map(DefStorage(_))

  implicit val encodeDefSchemaStorage: Encoder[DefSchemaStorage] = (x: DefSchemaStorage) => x.repr

  //implicitly[Decoder[TaskInfo]]

  //  implicit val decodeKeyPath: KeyDecoder[Path] = (key: Path) => Some(key)
  //

  //
  //  implicit val decodeRepr: Decoder[JsonRepr] = Decoder.decodeMapLike(
  //    dk = decodeKeyPath,
  //    dv = decodeAnySimpleDef,
  //    cbf = Map.canBuildFrom)

  implicit val encodeStringDef: Encoder[StringDef] = (x: StringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.StringTypeName.asJson))

  implicit val encodeIntDef: Encoder[IntDef] = (x: IntDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.IntTypeName.asJson))

  implicit val encodeBooleanDef: Encoder[BooleanDef] = (x: BooleanDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.BooleanTypeName.asJson))

  implicit val encodeDecimalDef: Encoder[DecimalDef] = (x: DecimalDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("index", x.index.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.DecimalTypeName.asJson))

  implicit val encodeObjectDef: Encoder[ObjectDef] = (x: ObjectDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("mutable", x.mutable.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ObjectTypeName.asJson))

  implicit val encodeMLStringDef: Encoder[MLStringDef] = (x: MLStringDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.MLStringTypeName.asJson))

  implicit val encodeArrayDef: Encoder[ArrayDef] = (x: ArrayDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.ArrayTypeName.asJson))

  implicit val encodeRootDef: Encoder[RootDef] = (x: RootDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("mutable", x.mutable.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RootTypeName.asJson))

  implicit val encodeRoleDef: Encoder[RoleDef] = (x: RoleDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleTypeName.asJson))

  implicit val encodeRoleGroupDef: Encoder[RoleGroupDef] = (x: RoleGroupDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RoleGroupTypeName.asJson))

  implicit val encodeRolesDef: Encoder[RolesDef] = (x: RolesDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RolesTypeName.asJson))

  implicit val encodeRefDef: Encoder[RefDef] = (x: RefDef) => Json.obj(
    ("name", x.name.asJson),
    ("description", x.description.asJson),
    ("value", x.value.asJson),
    ("path", x.path.asJson),
    ("type", Def.RefTypeName.asJson))

  implicit val encodeActorId: Encoder[ActorId] = (x: ActorId) => x.raw.asJson
  implicit val decodeActorId: Decoder[ActorId] = Decoder.decodeString.map(ActorId.apply)

  implicit val encodePath: Encoder[Path] = (x: Path) => x.raw.asJson
  implicit val decodePath: Decoder[Path] = Decoder.decodeString.map(Path.apply)

  implicit val encodeDataPath: Encoder[DataPath] = (x: DataPath) => x.raw.asJson
  implicit val decodeDataPath: Decoder[DataPath] = Decoder.decodeString.map(DataPath.apply)

  //  implicit val encodeDataElement: Encoder[DataElement] = (exception: DataElement) => Json.obj(
  //    ("path", exception.path.asJson),
  //    ("value", exception.value.asJson))
  //
  //  implicit val decodeDataElement: Decoder[DataElement] = (c: HCursor) => for {
  //    path <- c.downField("path").as[String]
  //    value <- c.downField("value").as[Any]
  //  } yield DataElement(path, value)
  //

  implicit val encodeDef: Encoder[Def] = {
    case x: StringDef => x.asJson
    case x: IntDef => x.asJson
    case x: BooleanDef => x.asJson
    case x: DecimalDef => x.asJson
    case x: ObjectDef => x.asJson
    case x: MLStringDef => x.asJson
    case x: ArrayDef => x.asJson
    case x: RootDef => x.asJson
    case x: RoleDef => x.asJson
    case x: RoleGroupDef => x.asJson
    case x: RolesDef => x.asJson
    case x: RefDef => x.asJson
  }

  implicit val decodeStringDef: Decoder[StringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[String]
    fPath <- c.downField("path").as[DataPath]
  } yield StringDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeIntDef: Decoder[IntDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[Int]
    fPath <- c.downField("path").as[DataPath]
  } yield IntDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeBooleanDef: Decoder[BooleanDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[Boolean]
    fPath <- c.downField("path").as[DataPath]
  } yield BooleanDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeDecimalDef: Decoder[DecimalDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fIndex <- c.downField("index").as[Option[DataPath]]
    fValue <- c.downField("value").as[BigDecimal]
    fPath <- c.downField("path").as[DataPath]
  } yield DecimalDef(name = fName, description = fDescription, index = fIndex, value = fValue, path = fPath)

  implicit val decodeObjectDef: Decoder[ObjectDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fMutable <- c.downField("mutable").as[Boolean]
    fValue <- c.downField("value").as[Map[String, Def]]
    fPath <- c.downField("path").as[DataPath]
  } yield ObjectDef(name = fName, description = fDescription, mutable = fMutable, value = fValue, path = fPath)

  implicit val decodeMLStringDef: Decoder[MLStringDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Map[String, StringDef]]
    fPath <- c.downField("path").as[DataPath]
  } yield MLStringDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeArrayDef: Decoder[ArrayDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[List[Def]]
    fPath <- c.downField("path").as[DataPath]
  } yield ArrayDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeRootDef: Decoder[RootDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fMutable <- c.downField("mutable").as[Boolean]
    fValue <- c.downField("value").as[Map[String, Def]]
    fPath <- c.downField("path").as[DataPath]
  } yield RootDef(name = fName, description = fDescription, mutable = fMutable, value = fValue, path = fPath)

  implicit val decodeRoleDef: Decoder[RoleDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[UUID]
    fPath <- c.downField("path").as[DataPath]
  } yield RoleDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeRoleGroupDef: Decoder[RoleGroupDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Map[String, RoleDef]]
    fPath <- c.downField("path").as[DataPath]
  } yield RoleGroupDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeRolesDef: Decoder[RolesDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Map[String, RoleGroupDef]]
    fPath <- c.downField("path").as[DataPath]
  } yield RolesDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeRefDef: Decoder[RefDef] = (c: HCursor) => for {
    fName <- c.downField("name").as[Name]
    fDescription <- c.downField("description").as[Description]
    fValue <- c.downField("value").as[Path]
    fPath <- c.downField("path").as[DataPath]
  } yield RefDef(name = fName, description = fDescription, value = fValue, path = fPath)

  implicit val decodeDef: Decoder[Def] = (c: HCursor) => (for {
    fType <- c.downField("type").as[String]
  } yield fType).flatMap {
    case Def.StringTypeName => decodeStringDef(c)
    case Def.IntTypeName => decodeIntDef(c)
    case Def.BooleanTypeName => decodeBooleanDef(c)
    case Def.DecimalTypeName => decodeDecimalDef(c)
    case Def.ObjectTypeName => decodeObjectDef(c)
    case Def.MLStringTypeName => decodeMLStringDef(c)
    case Def.ArrayTypeName => decodeArrayDef(c)
    case Def.RootTypeName => decodeRootDef(c)
    case Def.RoleTypeName => decodeRoleDef(c)
    case Def.RoleGroupTypeName => decodeRoleGroupDef(c)
    case Def.RolesTypeName => decodeRolesDef(c)
    case Def.RefTypeName => decodeRefDef(c)
  }
}