package sphera.bpm.masterdata.model

import java.util.UUID

import sphera.core.domain.tenancy.model.User

case class Characteristic(
  id: Characteristic.Id,
  name: String,
  description: Option[String],
  values: Map[Characteristic.ValueId, String],
  modifyAttr: ModifyAttr)

object Characteristic {
  type Id = UUID
  type Characteristics = Map[Characteristic.Id, Characteristic]
  type ValueId = UUID
  type CharacteristicAssignments = Map[Id, ValueId]
}

case class UpdateCharacteristic(
  id: Characteristic.Id,
  name: String,
  description: Option[String],
  values: Map[Characteristic.ValueId, String],
  userId: User.Id)

