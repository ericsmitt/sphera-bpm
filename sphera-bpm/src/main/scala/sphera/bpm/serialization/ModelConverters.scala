//package sphera.bpm.serialization
//
//import sphera.bpm.model.Role.RoleDefs
//import sphera.bpm.model._
//import sphera.bpm.serializer.proto.model._
//
//trait ModelConverters {
//  import Implicits._
//
//  implicit def toModifyAttr(x: ModifyAttr): ModifyAttrV1 = {
//    ModifyAttrV1(
//      createdBy = toModifier(x.createdBy),
//      createdOn = x.createdOn.toString,
//      updatedBy = x.updatedBy.map(toModifier),
//      updatedOn = x.updatedOn.map(_.toString))
//  }
//
//  implicit def fromModifyAttr(x: ModifyAttrV1): ModifyAttr = {
//    ModifyAttr(
//      createdBy = fromModifier(x.createdBy),
//      createdOn = stringToZonedDateTime(x.createdOn),
//      updatedBy = x.updatedBy.map(fromModifier),
//      updatedOn = stringOptToZonedDateTimeOpt(x.updatedOn))
//  }
//
//  implicit def toModifier(x: Modifier): ModifierV1 = {
//    ModifierV1(
//      actorId = x.actorId,
//      userId = UUIDOptToStringOpt(x.userId),
//      modifierType = toModifierType(x.modifierType))
//  }
//
//  implicit def fromModifier(x: ModifierV1): Modifier = {
//    Modifier(
//      actorId = x.actorId,
//      userId = stringOptToUUIDOpt(x.userId),
//      modifierType = fromModifierType(x.modifierType))
//  }
//
//  implicit def toModifierType(x: ModifierType): Int = {
//    x match {
//      case ModifierType.User => 1
//      case ModifierType.Operation => 2
//      case ModifierType.Process => 3
//      case ModifierType.Task => 4
//    }
//  }
//
//  implicit def fromModifierType(x: Int): ModifierType = {
//    x match {
//      case 1 => ModifierType.User
//      case 2 => ModifierType.Operation
//      case 3 => ModifierType.Process
//      case 4 => ModifierType.Task
//    }
//  }
//
//  implicit def toEntityType(x: EntityType): Int = {
//    x match {
//      case EntityType.ProjectInfo => 1
//      case EntityType.ProcessInfo => 2
//      case EntityType.TaskInfo => 3
//      case EntityType.DataStructure => 4
//      case EntityType.ProjectTemplate => 5
//      case EntityType.ProcessTemplate => 6
//      case EntityType.FormTemplate => 7
//      case EntityType.RoleType => 8
//      case EntityType.FileType => 9
//      case EntityType.Characteristic => 10
//      case EntityType.CalendarType => 11
//      case EntityType.ActorRef => 12
//      case EntityType.HistoryData => 13
//      case _ => 1
//    }
//  }
//
//  implicit def fromEntityType(x: Int): EntityType = {
//    x match {
//      case 1 => EntityType.ProjectInfo
//      case 2 => EntityType.ProcessInfo
//      case 3 => EntityType.TaskInfo
//      case 4 => EntityType.DataStructure
//      case 5 => EntityType.ProjectTemplate
//      case 6 => EntityType.ProcessTemplate
//      case 7 => EntityType.FormTemplate
//      case 8 => EntityType.RoleType
//      case 9 => EntityType.FileType
//      case 10 => EntityType.Characteristic
//      case 11 => EntityType.CalendarType
//      case 12 => EntityType.ActorRef
//      case 13 => EntityType.HistoryData
//      case _ => EntityType.ProcessInfo
//    }
//  }
//
//  implicit def toRoleDefinitions(x: RoleDefs): Map[String, RoleDefinitionV1] = {
//    x.mapValues(toRoleDefinition)
//  }
//
//  implicit def fromRoleDefinitions(x: Map[String, RoleDefinitionV1]): RoleDefs = {
//    x.mapValues(fromRoleDefinition)
//  }
//
//  implicit def toRoleDefinition(x: RoleDef): RoleDefinitionV1 = {
//    RoleDefinitionV1(
//      name = x.name,
//      description = x.description,
//      roleTypeId = x.roleTypeId,
//      userId = x.userId)
//  }
//
//  implicit def fromRoleDefinition(x: RoleDefinitionV1): RoleDef = {
//    RoleDef(
//      name = x.name,
//      description = x.description,
//      roleTypeId = stringToUUID(x.roleTypeId),
//      userId = x.userId)
//  }
//}