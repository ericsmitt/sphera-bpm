package sphera.bpm.model

sealed trait ModifierType

object ModifierType {
  case object User extends ModifierType
  case object Operation extends ModifierType
  case object Task extends ModifierType
  case object Process extends ModifierType
}
