package sphera.bpm.masterdata.model

import java.util.UUID

import sphera.bpm.masterdata.model.Characteristic.CharacteristicAssignments
import sphera.core.domain.tenancy.model.User

case class RoleType(
  id: RoleType.Id,
  name: String,
  description: Option[String],
  users: Set[User.Id],
  classification: CharacteristicAssignments,
  modifyAttr: ModifyAttr)

object RoleType {
  type Id = UUID
  type RoleTypes = Map[RoleType.Id, RoleType]

  //  val ProcessManager = RoleType(
  //    UUID.fromString("996bea11-5998-4d3a-acb3-05216f430205"),
  //    "Менеджер процесса",
  //    None,
  //    Set.empty
  //  )
  //
  //  val processManagerName = "Менеджер процесса"
  //  val
}

case class UpdateRoleType(
  id: RoleType.Id,
  name: String,
  description: Option[String],
  users: Set[User.Id],
  classification: CharacteristicAssignments,
  userId: User.Id)

