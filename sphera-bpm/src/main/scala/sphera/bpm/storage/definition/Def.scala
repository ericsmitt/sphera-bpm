package sphera.bpm.storage.definition

import sphera.bpm.json._
import sphera.bpm.storage.definition.DefType._
import sphera.core.akkaext.actor._
import sphera.core.domain.tenancy.model.User
import io.circe.Json

trait Def {
  def name: Name
  def description: Description
  def value: Value
  def path: DataPath

  /**
   * The catamorphism for the Def value data type.
   */
  def fold[A](
    ifIntDef: IntDef => A,
    ifStringDef: StringDef => A,
    ifBooleanDef: BooleanDef => A,
    ifDecimalDef: DecimalDef => A,
    ifRefDef: RefDef => A,
    ifRoleDef: RoleDef => A,
    ifRoleGroupDef: RoleGroupDef => A,
    ifRolesDef: RolesDef => A,
    ifMLStringDef: MLStringDef => A,
    ifArrayDef: ArrayDef => A,
    ifObjectDef: ObjectDef => A,
    ifRootDef: RootDef => A): A = this match {
    case x: IntDef => ifIntDef(x)
    case x: StringDef => ifStringDef(x)
    case x: BooleanDef => ifBooleanDef(x)
    case x: DecimalDef => ifDecimalDef(x)
    case x: RefDef => ifRefDef(x)
    case x: RoleDef => ifRoleDef(x)
    case x: RoleGroupDef => ifRoleGroupDef(x)
    case x: RolesDef => ifRolesDef(x)
    case x: MLStringDef => ifMLStringDef(x)
    case x: ArrayDef => ifArrayDef(x)
    case x: ObjectDef => ifObjectDef(x)
    case x: RootDef => ifRootDef(x)
  }

  def foldObjectLike[A](
    ifRoleGroupDef: RoleGroupDef => A,
    ifRolesDef: RolesDef => A,
    ifMLStringDef: MLStringDef => A,
    ifObjectDef: ObjectDef => A,
    ifRootDef: RootDef => A): A = this match {
    case x: RoleGroupDef => ifRoleGroupDef(x)
    case x: RolesDef => ifRolesDef(x)
    case x: MLStringDef => ifMLStringDef(x)
    case x: ObjectDef => ifObjectDef(x)
    case x: RootDef => ifRootDef(x)
  }

  def foldRef[A](ifRefDef: RefDef => A, ifDef: Def => A): A = this match {
    case x: RefDef => ifRefDef(x)
    case x: Def => ifDef(x)
  }

  override def toString = this.asJson.spaces2
}

abstract class DefType(val value: String) {
  /**
   * The catamorphism for the DefType value data type.
   */
  def fold[A](
    ifIntType: => A,
    ifStringType: => A,
    ifBooleanType: => A,
    ifDecimalType: => A,
    ifRefType: => A,
    ifRoleType: => A,
    ifRoleGroupType: => A,
    ifRolesType: => A,
    ifMLStringType: => A,
    ifArrayType: => A,
    ifObjectType: => A,
    ifRootType: => A): A = this match {
    case IntType => ifIntType
    case StringType => ifStringType
    case BooleanType => ifBooleanType
    case DecimalType => ifDecimalType
    case RefType => ifRefType
    case RoleType => ifRoleType
    case RoleGroupType => ifRoleGroupType
    case RolesType => ifRolesType
    case MLStringType => ifMLStringType
    case ArrayType => ifArrayType
    case ObjectType => ifObjectType
    case RootType => ifRootType
  }

  override def toString = value
}

object DefType {
  case object IntType extends DefType("int")
  case object StringType extends DefType("string")
  case object BooleanType extends DefType("boolean")
  case object DecimalType extends DefType("decimal")
  case object RoleType extends DefType("role")
  case object RoleGroupType extends DefType("roleGroup")
  case object RolesType extends DefType("roles")
  case object MLStringType extends DefType("MLString")
  case object ArrayType extends DefType("array")
  case object ObjectType extends DefType("object")
  case object RootType extends DefType("root")
  case object RefType extends DefType("ref")

  def apply(x: String): DefType = x match {
    case IntType.value => IntType
    case StringType.value => StringType
    case BooleanType.value => BooleanType
    case DecimalType.value => DecimalType
    case RoleType.value => RoleType
    case RoleGroupType.value => RoleGroupType
    case RolesType.value => RolesType
    case MLStringType.value => MLStringType
    case ArrayType.value => ArrayType
    case ObjectType.value => ObjectType
    case RootType.value => RootType
    case RefType.value => RefType
  }
}

object Def {
  val IntTypeName = "int"
  val StringTypeName = "string"
  val BooleanTypeName = "boolean"
  val DecimalTypeName = "decimal"
  val RoleTypeName = "role"
  val RolesTypeName = "roles"
  val RoleGroupTypeName = "roleGroup"
  val RefTypeName = "ref"
  val MLStringTypeName = "mlString"
  val ArrayTypeName = "array"
  val ObjectTypeName = "object"
  val RootTypeName = "root"
}

trait ComplexDef extends Def

case class BooleanDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: Boolean,
  path: DataPath) extends Def

case class IntDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: Int,
  path: DataPath) extends Def

case class DecimalDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: BigDecimal,
  path: DataPath) extends Def

case class StringDef(
  name: Name,
  description: Description,
  index: Option[DataPath],
  value: String,
  path: DataPath) extends Def

case class RoleDef(
  name: Name,
  description: Description,
  value: User.Id,
  path: DataPath) extends ComplexDef

case class RoleGroupDef(
  name: Name,
  description: Description,
  value: Map[String, RoleDef],
  path: DataPath) extends ComplexDef

case class RolesDef(
  name: Name,
  description: Description,
  value: Map[String, RoleGroupDef],
  path: DataPath) extends ComplexDef

case class RefDef(
  name: Name,
  description: Description,
  value: Path,
  path: DataPath) extends ComplexDef

case class MLStringDef(
  name: Name,
  description: Description,
  value: Map[String, StringDef],
  path: DataPath) extends ComplexDef

case class ArrayDef(
  name: Name,
  description: Description,
  value: List[Def],
  path: DataPath) extends ComplexDef

object ArrayDef {
  def fromObject(path: DataPath, obj: Vector[(String, Json)]): Option[ArrayDef] = {
    val elements = obj
      .toList
      .map(_._2)
      .map(_.as[Def])
      .flatMap(_.toOption)

    if (elements.isEmpty) None else {
      Some(ArrayDef(
        name = path.name,
        description = None,
        value = elements,
        path = path))
    }
  }
}

trait ObjectLikeDef extends Def {
  def mutable: Boolean
  def value: Map[String, Def]

  def withValue(value: Map[String, Def]) = this match {
    case x: ObjectDef => x.copy(value = value)
    case x: RootDef => x.copy(value = value)
  }
}

case class ObjectDef(
  name: Name,
  description: Description,
  mutable: Boolean,
  value: Map[String, Def],
  path: DataPath) extends ObjectLikeDef with ComplexDef

case class RootDef(
  name: Name = "root",
  description: Description = Some("root"),
  mutable: Boolean,
  value: Map[String, Def] = Map[String, Def](),
  path: DataPath = RootDataPath) extends ObjectLikeDef with ComplexDef

object RootDef {
  def empty(mutable: Boolean = false) = RootDef(mutable = mutable)

  def apply(x: (String, Def)*): RootDef = RootDef(mutable = false, value = Map(x: _*))

  def apply(x: ObjectDef): RootDef = RootDef(
    name = x.name,
    description = x.description,
    mutable = x.mutable,
    value = x.value)
}

