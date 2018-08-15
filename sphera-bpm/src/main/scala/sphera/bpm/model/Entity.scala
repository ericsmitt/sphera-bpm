package sphera.bpm.model

object Entity {
  type Id = String
}

sealed trait EntityType

object EntityType {
  case object ProjectInfo extends EntityType
  case object ProcessInfo extends EntityType
  case object TaskInfo extends EntityType
  case object DataStructure extends EntityType
  case object ProjectTemplate extends EntityType
  case object ProcessTemplate extends EntityType
  case object FormTemplate extends EntityType
  case object RoleType extends EntityType
  case object FileType extends EntityType
  case object Characteristic extends EntityType
  case object CalendarType extends EntityType
  case object Verification extends EntityType
  case object ActorRef extends EntityType
  case object HistoryData extends EntityType
}
