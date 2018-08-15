package sphera.bpm.json.storage.definition

import java.util.UUID

import sphera.bpm.Implicits._
import sphera.bpm.storage.definition.{ PatchDef, _ }
import sphera.core.akkaext.actor.{ DataPath, Path }
import io.circe._
import sphera.bpm.Implicits._
import sphera.bpm.json._

trait PatchDefCodec extends ReadDefCodec with CreateDefCodec with UpdateDefCodec with DeleteDefCodec {
  implicit val encodePatchDef: Encoder[PatchDef] = {
    case x: ReadDef => x.asJson
    case x: CreateDef => x.asJson
    case x: DeleteDef => x.asJson
    case x: UpdateDef => x.asJson
  }

  implicit val encodePatchStringDef: Encoder[PatchStringDef] = {
    case x: ReadDef => x.asJson
    case x: CreateStringDef => x.asJson
    case x: UpdateStringDef => x.asJson
    case x: DeleteStringDef => x.asJson
  }

  implicit val encodePatchRoleDef: Encoder[PatchRoleDef] = {
    case x: ReadDef => x.asJson
    case x: CreateRoleDef => x.asJson
    case x: UpdateRoleDef => x.asJson
    case x: DeleteRoleDef => x.asJson
  }

  implicit val encodePatchRoleGroupDef: Encoder[PatchRoleGroupDef] = {
    case x: ReadDef => x.asJson
    case x: CreateRoleGroupDef => x.asJson
    case x: UpdateRoleGroupDef => x.asJson
    case x: DeleteRoleGroupDef => x.asJson
  }

  implicit class ACursorOpt(x: ACursor) {
    def asPatchOpt = x.as[Option[String]].map(_.map(Patch.apply))
    def asType = x.as[String].map(DefType.apply)
  }

  implicit def mapToReadDef[T <: Def](decoder: Decoder.Result[T]): Decoder.Result[ReadDef] = {
    decoder.map(ReadDef.apply)
  }

  implicit val decodePatchDef: Decoder[PatchDef] = (c: HCursor) => (for {
    fPatch <- c.downField("patch").asPatchOpt
    fType <- c.downField("type").asType
  } yield (fPatch, fType)).flatMap {
    case (fPatch, fType) => fPatch match {
      case Some(x) => x.fold(
        decodeUpdateDef(c),
        decodeCreateDef(c),
        decodeUpdateDef(c),
        decodeDeleteDef(c),
        decodeUpdateDef(c))
      case _ =>
        // if type field not defined map it to partial update AST
        // check if json document has any patch field
        val isPatch = c.focus.get.toString() contains "patch"
        if (isPatch) mapToUpdateDef(fType, c)
        else fType.fold(
          decodeIntDef(c),
          decodeStringDef(c),
          decodeBooleanDef(c),
          decodeDecimalDef(c),
          decodeRefDef(c),
          decodeRoleDef(c),
          decodeRoleGroupDef(c),
          decodeRolesDef(c),
          decodeMLStringDef(c),
          decodeArrayDef(c),
          decodeObjectDef(c),
          decodeRootDef(c))
    }
  }

  implicit val decodePatchStringDef: Decoder[PatchStringDef] = (c: HCursor) => (for {
    fPatch <- c.downField("patch").as[Option[String]].map(_.map(Patch.apply))
  } yield fPatch).flatMap {
    case None => decodeStringDef(c)
    case Some(Patch.Create) => decodeCreateStringDef(c)
    case Some(Patch.Update) => decodeUpdateStringDef(c)
    case Some(Patch.Delete) => decodeDeleteStringDef(c)
  }

  implicit val decodePatchRoleDef: Decoder[PatchRoleDef] = (c: HCursor) => (for {
    fPatch <- c.downField("patch").as[Option[String]].map(_.map(Patch.apply))
  } yield fPatch).flatMap {
    case None => decodeRoleDef(c)
    case Some(Patch.Create) => decodeCreateRoleDef(c)
    case Some(Patch.Update) => decodeUpdateRoleDef(c)
    case Some(Patch.Delete) => decodeDeleteRoleDef(c)
  }

  implicit val decodePatchRoleGroupDefOrRoleGroupDef: Decoder[PatchRoleGroupDef] = (c: HCursor) => (for {
    fPatch <- c.downField("patch").as[Option[String]].map(_.map(Patch.apply))
  } yield fPatch).flatMap {
    case None => decodeRoleGroupDef(c)
    case Some(Patch.Create) => decodeCreateRoleGroupDef(c)
    case Some(Patch.Update) => decodeUpdateRoleGroupDef(c)
    case Some(Patch.Delete) => decodeDeleteRoleGroupDef(c)
  }
}