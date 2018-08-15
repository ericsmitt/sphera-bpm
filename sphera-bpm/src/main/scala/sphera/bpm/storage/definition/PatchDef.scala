package sphera.bpm.storage.definition

import sphera.bpm.json._
import sphera.bpm.storage.definition.Patch._
import sphera.core.akkaext.actor._
import sphera.core.domain.tenancy.model.User

trait PatchDef {
  /**
   * The catamorphism for the PatchDef value data type.
   */
  def fold[A](
    ifReadDef: ReadDef => A,
    ifCreateDef: CreateDef => A,
    ifUpdateDef: UpdateDef => A,
    ifDeleteDef: DeleteDef => A): A = this match {
    case x: ReadDef => ifReadDef(x)
    case x: CreateDef => ifCreateDef(x)
    case x: UpdateDef => ifUpdateDef(x)
    case x: DeleteDef => ifDeleteDef(x)
  }

  def foldRoleGroup[A](
    ifReadDef: RoleGroupDef => A,
    ifCreateRoleGroupDef: CreateRoleGroupDef => A,
    ifUpdateRoleGroupDef: UpdateRoleGroupDef => A,
    ifDeleteRoleGroupDef: DeleteRoleGroupDef => A): A = this match {
    case ReadDef(x: RoleGroupDef) => ifReadDef(x)
    case x: CreateRoleGroupDef => ifCreateRoleGroupDef(x)
    case x: UpdateRoleGroupDef => ifUpdateRoleGroupDef(x)
    case x: DeleteRoleGroupDef => ifDeleteRoleGroupDef(x)
  }

  def foldRoles[A](
    ifReadDef: RolesDef => A,
    ifCreateRolesDef: CreateRolesDef => A,
    ifUpdateRolesDef: UpdateRolesDef => A,
    ifDeleteRolesDef: DeleteRolesDef => A): A = this match {
    case ReadDef(x: RolesDef) => ifReadDef(x)
    case x: CreateRolesDef => ifCreateRolesDef(x)
    case x: UpdateRolesDef => ifUpdateRolesDef(x)
    case x: DeleteRolesDef => ifDeleteRolesDef(x)
  }

  def foldMLString[A](
    ifReadDef: MLStringDef => A,
    ifCreateMLStringDef: CreateMLStringDef => A,
    ifUpdateMLStringDef: UpdateMLStringDef => A,
    ifDeleteMLStringDef: DeleteMLStringDef => A): A = this match {
    case ReadDef(x: MLStringDef) => ifReadDef(x)
    case x: CreateMLStringDef => ifCreateMLStringDef(x)
    case x: UpdateMLStringDef => ifUpdateMLStringDef(x)
    case x: DeleteMLStringDef => ifDeleteMLStringDef(x)
  }

  def foldArray[A](
    ifReadDef: ArrayDef => A,
    ifCreateArrayDef: CreateArrayDef => A,
    ifUpdateArrayDef: UpdateArrayDef => A,
    ifDeleteArrayDef: DeleteArrayDef => A): A = this match {
    case ReadDef(x: ArrayDef) => ifReadDef(x)
    case x: CreateArrayDef => ifCreateArrayDef(x)
    case x: UpdateArrayDef => ifUpdateArrayDef(x)
    case x: DeleteArrayDef => ifDeleteArrayDef(x)
  }

  def foldObject[A](
    ifReadDef: ObjectDef => A,
    ifCreateObjectDef: CreateObjectDef => A,
    ifUpdateObjectDef: UpdateObjectDef => A,
    ifDeleteObjectDef: DeleteObjectDef => A): A = this match {
    case ReadDef(x: ObjectDef) => ifReadDef(x)
    case x: CreateObjectDef => ifCreateObjectDef(x)
    case x: UpdateObjectDef => ifUpdateObjectDef(x)
    case x: DeleteObjectDef => ifDeleteObjectDef(x)
  }

  def foldRoot[A](
    ifReadDef: RootDef => A,
    ifCreateRootDef: CreateRootDef => A,
    ifUpdateRootDef: UpdateRootDef => A,
    ifDeleteRootDef: DeleteRootDef => A): A = this match {
    case ReadDef(x: RootDef) => ifReadDef(x)
    case x: CreateRootDef => ifCreateRootDef(x)
    case x: UpdateRootDef => ifUpdateRootDef(x)
    case x: DeleteRootDef => ifDeleteRootDef(x)
  }
}

case class ReadDef(value: Def) extends PatchDef
  with PatchStringDef
  with PatchRoleDef
  with PatchRoleGroupDef {

  def fold[A](
    ifReadIntDef: IntDef => A,
    ifReadStringDef: StringDef => A,
    ifReadBooleanDef: BooleanDef => A,
    ifReadDecimalDef: DecimalDef => A,
    ifReadRefDef: RefDef => A,
    ifReadRoleDef: RoleDef => A,
    ifReadRoleGroupDef: RoleGroupDef => A,
    ifReadRolesDef: RolesDef => A,
    ifReadMLStringDef: MLStringDef => A,
    ifReadArrayDef: ArrayDef => A,
    ifReadObjectDef: ObjectDef => A,
    ifReadRootDef: RootDef => A): A = value.fold(
    intDef => ifReadIntDef(intDef),
    stringDef => ifReadStringDef(stringDef),
    booleanDef => ifReadBooleanDef(booleanDef),
    decimalDef => ifReadDecimalDef(decimalDef),
    refDef => ifReadRefDef(refDef),
    roleDef => ifReadRoleDef(roleDef),
    roleGroupDef => ifReadRoleGroupDef(roleGroupDef),
    rolesDef => ifReadRolesDef(rolesDef),
    mlStringDef => ifReadMLStringDef(mlStringDef),
    arrayDef => ifReadArrayDef(arrayDef),
    objectDef => ifReadObjectDef(objectDef),
    rootDef => ifReadRootDef(rootDef))
}

trait CreateDef extends PatchDef {
  def name: Name
  def description: Description
  def value: Value
  def path: Option[DataPath]

  def fold[A](
    ifCreateIntDef: CreateIntDef => A,
    ifCreateStringDef: CreateStringDef => A,
    ifCreateBooleanDef: CreateBooleanDef => A,
    ifCreateDecimalDef: CreateDecimalDef => A,
    ifCreateRefDef: CreateRefDef => A,
    ifCreateRoleDef: CreateRoleDef => A,
    ifCreateRoleGroupDef: CreateRoleGroupDef => A,
    ifCreateRolesDef: CreateRolesDef => A,
    ifCreateMLStringDef: CreateMLStringDef => A,
    ifCreateArrayDef: CreateArrayDef => A,
    ifCreateObjectDef: CreateObjectDef => A,
    ifCreateRootDef: CreateRootDef => A): A = this match {
    case x: CreateIntDef => ifCreateIntDef(x)
    case x: CreateStringDef => ifCreateStringDef(x)
    case x: CreateBooleanDef => ifCreateBooleanDef(x)
    case x: CreateDecimalDef => ifCreateDecimalDef(x)
    case x: CreateRefDef => ifCreateRefDef(x)
    case x: CreateRoleDef => ifCreateRoleDef(x)
    case x: CreateRoleGroupDef => ifCreateRoleGroupDef(x)
    case x: CreateRolesDef => ifCreateRolesDef(x)
    case x: CreateMLStringDef => ifCreateMLStringDef(x)
    case x: CreateArrayDef => ifCreateArrayDef(x)
    case x: CreateObjectDef => ifCreateObjectDef(x)
    case x: CreateRootDef => ifCreateRootDef(x)
  }

  override def toString = this.asJson.spaces2
}

trait UpdateDef extends PatchDef {
  def name: Option[Name]
  def description: Option[Description]
  def value: Option[Value]
  def path: Option[DataPath]

  def fold[A](
    ifUpdateIntDef: UpdateIntDef => A,
    ifUpdateStringDef: UpdateStringDef => A,
    ifUpdateBooleanDef: UpdateBooleanDef => A,
    ifUpdateDecimalDef: UpdateDecimalDef => A,
    ifUpdateRefDef: UpdateRefDef => A,
    ifUpdateRoleDef: UpdateRoleDef => A,
    ifUpdateRoleGroupDef: UpdateRoleGroupDef => A,
    ifUpdateRolesDef: UpdateRolesDef => A,
    ifUpdateMLStringDef: UpdateMLStringDef => A,
    ifUpdateArrayDef: UpdateArrayDef => A,
    ifUpdateObjectDef: UpdateObjectDef => A,
    ifUpdateRootDef: UpdateRootDef => A): A = this match {
    case x: UpdateIntDef => ifUpdateIntDef(x)
    case x: UpdateStringDef => ifUpdateStringDef(x)
    case x: UpdateBooleanDef => ifUpdateBooleanDef(x)
    case x: UpdateDecimalDef => ifUpdateDecimalDef(x)
    case x: UpdateRefDef => ifUpdateRefDef(x)
    case x: UpdateRoleDef => ifUpdateRoleDef(x)
    case x: UpdateRoleGroupDef => ifUpdateRoleGroupDef(x)
    case x: UpdateRolesDef => ifUpdateRolesDef(x)
    case x: UpdateMLStringDef => ifUpdateMLStringDef(x)
    case x: UpdateArrayDef => ifUpdateArrayDef(x)
    case x: UpdateObjectDef => ifUpdateObjectDef(x)
    case x: UpdateRootDef => ifUpdateRootDef(x)
  }

  def foldComplex[A](
    ifUpdateRoleGroupDef: UpdateRoleGroupDef => A,
    ifUpdateRolesDef: UpdateRolesDef => A,
    ifUpdateMLStringDef: UpdateMLStringDef => A,
    ifUpdateArrayDef: UpdateArrayDef => A,
    ifUpdateObjectDef: UpdateObjectDef => A,
    ifUpdateRootDef: UpdateRootDef => A): A = this match {
    case x: UpdateRoleGroupDef => ifUpdateRoleGroupDef(x)
    case x: UpdateRolesDef => ifUpdateRolesDef(x)
    case x: UpdateMLStringDef => ifUpdateMLStringDef(x)
    case x: UpdateArrayDef => ifUpdateArrayDef(x)
    case x: UpdateObjectDef => ifUpdateObjectDef(x)
    case x: UpdateRootDef => ifUpdateRootDef(x)
  }

  override def toString = this.asJson.spaces2
}

trait UpdateComplexDef extends UpdateDef {
  def value: Option[Map[String, PatchDef]]
  def patch: Option[Compound]
  def fold[A](
    ifUpdateRoleGroupDef: UpdateRoleGroupDef => A,
    ifUpdateRolesDef: UpdateRolesDef => A,
    ifUpdateMLStringDef: UpdateMLStringDef => A,
    ifUpdateArrayDef: UpdateArrayDef => A,
    ifUpdateObjectDef: UpdateObjectDef => A,
    ifUpdateRootDef: UpdateRootDef => A): A = this match {
    case x: UpdateRoleGroupDef => ifUpdateRoleGroupDef(x)
    case x: UpdateRolesDef => ifUpdateRolesDef(x)
    case x: UpdateMLStringDef => ifUpdateMLStringDef(x)
    case x: UpdateArrayDef => ifUpdateArrayDef(x)
    case x: UpdateObjectDef => ifUpdateObjectDef(x)
    case x: UpdateRootDef => ifUpdateRootDef(x)
  }
}

trait DeleteDef extends PatchDef {
  def name: Option[Name]
  def description: Option[Description]
  def value: Option[Value]
  def path: Option[DataPath]

  override def toString = this.asJson.spaces2
}

case class CreateBooleanDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: Boolean,
  path: Option[DataPath] = None) extends CreateDef

case class UpdateBooleanDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[Boolean],
  path: Option[DataPath] = None) extends UpdateDef

case class DeleteBooleanDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[Boolean],
  path: Option[DataPath] = None) extends DeleteDef

case class CreateIntDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: Int,
  path: Option[DataPath] = None) extends CreateDef

case class UpdateIntDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[Int],
  path: Option[DataPath] = None) extends UpdateDef

case class DeleteIntDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[Int],
  path: Option[DataPath] = None) extends DeleteDef

case class CreateDecimalDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: BigDecimal,
  path: Option[DataPath] = None) extends CreateDef

case class UpdateDecimalDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[BigDecimal],
  path: Option[DataPath] = None) extends UpdateDef

case class DeleteDecimalDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[BigDecimal],
  path: Option[DataPath] = None) extends DeleteDef

trait PatchStringDef extends PatchDef

case class CreateStringDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: String,
  path: Option[DataPath] = None) extends CreateDef with PatchStringDef

case class UpdateStringDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[String],
  path: Option[DataPath] = None) extends UpdateDef with PatchStringDef

case class DeleteStringDef(
  name: Option[Name],
  description: Option[Description],
  index: Option[Option[DataPath]],
  value: Option[String],
  path: Option[DataPath] = None) extends DeleteDef with PatchStringDef

trait PatchRefDef extends PatchDef

case class CreateRefDef(
  name: Name,
  description: Description,
  value: Path,
  path: Option[DataPath] = None) extends CreateDef with PatchRefDef

case class UpdateRefDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[Path],
  path: Option[DataPath] = None) extends UpdateDef with PatchRefDef

case class DeleteRefDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[Path],
  path: Option[DataPath] = None) extends DeleteDef with PatchRefDef

trait PatchRoleDef extends PatchDef

case class CreateRoleDef(
  name: Name,
  description: Description,
  value: User.Id,
  path: Option[DataPath] = None) extends CreateDef with PatchRoleDef

case class UpdateRoleDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[User.Id],
  path: Option[DataPath] = None) extends UpdateDef with PatchRoleDef

case class DeleteRoleDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[User.Id],
  path: Option[DataPath] = None) extends DeleteDef with PatchRoleDef

trait PatchRoleGroupDef extends PatchDef

case class CreateRoleGroupDef(
  name: Name,
  description: Description,
  value: Map[String, RoleDef],
  path: Option[DataPath] = None) extends CreateDef with PatchRoleGroupDef

case class UpdateRoleGroupDef(
                               name: Option[Name],
                               description: Option[Description],
                               value: Option[Map[String, PatchRoleDef]],
                               patch: Option[Compound],
                               path: Option[DataPath] = None) extends UpdateComplexDef with PatchRoleGroupDef

case class DeleteRoleGroupDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[Map[String, PatchRoleDef]],
  path: Option[DataPath] = None) extends DeleteDef with PatchRoleGroupDef

case class CreateRolesDef(
  name: Name,
  description: Description,
  value: Map[String, RoleGroupDef],
  path: Option[DataPath] = None) extends CreateDef

case class UpdateRolesDef(
                           name: Option[Name],
                           description: Option[Description],
                           value: Option[Map[String, PatchRoleGroupDef]],
                           patch: Option[Compound],
                           path: Option[DataPath] = None) extends UpdateComplexDef

case class DeleteRolesDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[Map[String, PatchRoleGroupDef]],
  path: Option[DataPath] = None) extends DeleteDef

case class CreateMLStringDef(
  name: Name,
  description: Description,
  value: Map[String, StringDef],
  path: Option[DataPath] = None) extends CreateDef

case class UpdateMLStringDef(
                              name: Option[Name],
                              description: Option[Description],
                              value: Option[Map[String, PatchStringDef]],
                              patch: Option[Compound],
                              path: Option[DataPath] = None) extends UpdateComplexDef

case class DeleteMLStringDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[Map[String, PatchStringDef]],
  path: Option[DataPath] = None) extends DeleteDef
case class CreateArrayDef(
  name: Name,
  description: Description,
  value: List[Def],
  path: Option[DataPath] = None) extends CreateDef

case class UpdateArrayDef(
                           name: Option[Name],
                           description: Option[Description],
                           value: Option[List[PatchDef]],
                           patch: Option[Compound],
                           path: Option[DataPath] = None) extends UpdateDef

case class DeleteArrayDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[List[PatchDef]],
  path: Option[DataPath] = None) extends DeleteDef

case class CreateObjectDef(
  name: Name,
  description: Description,
  mutable: Boolean,
  value: Map[String, Def],
  path: Option[DataPath] = None) extends CreateDef

case class UpdateObjectDef(
                            name: Option[Name],
                            description: Option[Description],
                            value: Option[Map[String, PatchDef]],
                            patch: Option[Compound],
                            path: Option[DataPath] = None) extends UpdateComplexDef

case class DeleteObjectDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[Map[String, PatchDef]],
  path: Option[DataPath] = None) extends DeleteDef

case class CreateRootDef(
  name: Name,
  description: Description,
  mutable: Boolean,
  value: Map[String, Def],
  path: Option[DataPath] = None) extends CreateDef

case class UpdateRootDef(
                          name: Option[Name],
                          description: Option[Description],
                          value: Option[Map[String, PatchDef]],
                          patch: Option[Compound],
                          path: Option[DataPath] = None) extends UpdateComplexDef

case class DeleteRootDef(
  name: Option[Name],
  description: Option[Description],
  value: Option[Map[String, PatchDef]],
  path: Option[DataPath] = None) extends DeleteDef

abstract class Patch(val value: String) {
  /**
   * The catamorphism for the Patch value data type.
   */
  def fold[T](
    ifRead: => T,
    ifCreate: => T,
    ifUpdate: => T,
    ifDelete: => T,
    ifCompoundPatch: => T): T = this match {
    case Read => ifRead
    case Create => ifCreate
    case Update => ifUpdate
    case Delete => ifDelete
    case _ => ifCompoundPatch
  }

  override def toString = value
}

object Patch {
  case object Read extends Patch("read")
  case object Create extends Patch("create")
  case object Update extends Patch("update")
  case object Delete extends Patch("delete")

  abstract class UpdateMetadata(val value: String) {
    /**
      * The catamorphism for the UpdateMetadata value data type.
      */
    def fold[T](
                 ifRead: => T,
                 ifUpdate: => T,
                 ifAll: => T): T = this match {
      case UpdateMetadata.Update => ifUpdate
      case UpdateMetadata.All => ifAll
    }

    override def toString = value
  }

  object UpdateMetadata {
    case object Read extends UpdateMetadata("read")
    case object Update extends UpdateMetadata("update")
    case object All extends UpdateMetadata("all")

    def apply(value: String): UpdateMetadata = value match {
      case UpdateMetadata.Update.value => UpdateMetadata.Update
      case UpdateMetadata.All.value => UpdateMetadata.All
      case _ => UpdateMetadata.Read
    }
  }

  abstract class UpdateValue(val value: String) {
    /**
      * The catamorphism for the UpdateMetadata value data type.
      */
    def fold[T](
                 ifRead: => T,
                 ifAll: => T): T = this match {
      case UpdateValue.Read => ifRead
      case UpdateValue.All => ifAll
    }

    def apply(value: String): UpdateValue = value match {
      case UpdateValue.All.value => UpdateValue.All
      case _ => UpdateValue.Read
    }

    override def toString = value
  }

  object UpdateValue {
    case object Read extends UpdateValue("read")
    case object All extends UpdateValue("all")
  }

  case class Compound(updateMetadata: UpdateMetadata, updateValue: UpdateValue) extends Patch(s"$updateMetadata, $updateValue")

  object Compound {
    def apply(value: String): Compound = {
      val list = value
        .replaceAll(" ", "")
        .split(",")
        .toList

      list match {
        case x1 :: x2 :: Nil => Compound(updateMetadata = UpdateMetadata(x1), updateValue = UpdateValue(x2))
        case x1 :: Nil =>  Compound(updateMetadata = UpdateMetadata(x1), updateValue = UpdateValue.Read)
        case x => sys.error("Invalid CompoundPatch")
      }
    }
  }

  def apply(x: String): Patch = x match {
    case Create.value => Create
    case Update.value => Update
    case Delete.value => Delete
    case compound => Compound(compound)
  }
}
