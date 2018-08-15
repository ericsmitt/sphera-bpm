package sphera.bpm.masterdata.model

import java.util.UUID

import sphera.bpm.masterdata.model.DataStructure._
import sphera.bpm.masterdata.model.DataStructureElement._
import sphera.bpm.storage.Node
import sphera.core.akkaext.actor.Path
import sphera.core.domain.tenancy.model.User

case class DataStructure(
  id: DataStructure.Id,
  name: String,
  description: Option[String],
  elements: Map[String, DataStructureElement],
  mutable: Boolean,
  modifyAttr: ModifyAttr)

case class UpdateDataStructure(
  id: DataStructure.Id,
  name: Name,
  description: Description,
  elements: Map[String, DataStructureElement],
  mutable: Boolean,
  userId: User.Id)

trait DataStructureElement {
  def name: String
  def description: Option[String]

  checkName(name)
}

object DataStructure {
  type Id = UUID
  type Name = String
  type Description = Option[String]
}

object DataStructureElement {
  val namePattern = "^[a-zA-Z0-9_-]+$"

  def checkName(x: String): Unit = {
    require(
      x.matches(DataStructureElement.namePattern),
      "Имя переменной должно состоять из латинских символов, цифр и знаков подчёркивания, не может содержать пробел")
  }
}

case class IntDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[Int] = None,
  indexed: Boolean = false) extends DataStructureElement

case class StringDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[String] = None,
  indexed: Boolean = false) extends DataStructureElement

case class DecimalDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[BigDecimal] = None,
  indexed: Boolean = false) extends DataStructureElement

case class BooleanDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[Boolean] = None,
  indexed: Boolean = false) extends DataStructureElement

case class RoleDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[User.Id] = None, typeId: RoleType.Id) extends DataStructureElement

case class RoleGroupDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[Map[String, RoleDataStructureElement]]) extends DataStructureElement

case class RolesDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[Map[String, RoleGroupDataStructureElement]]) extends DataStructureElement

case class MLStringDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[Map[String, StringDataStructureElement]]) extends DataStructureElement

case class ObjectDataStructureElement(
  name: Name,
  description: Description,
  dataStructureId: DataStructure.Id) extends DataStructureElement

case class ArrayDataStructureElement(
  name: Name,
  description: Description,
  defaultValue: Option[List[DataStructure.Id]]) extends DataStructureElement

/**
 * Structure reference as [[Path]].
 */
case class RefDataStructureElement(
  name: Name,
  description: Description,
  dataStructureId: DataStructure.Id,
  defaultValue: Option[Path] = None) extends DataStructureElement
