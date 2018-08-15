//package sphera.bpm.http
//
//import sphera.bpm.Exception._
//import sphera.bpm.model.EntityType
//import sphera.core.exception.SpheraException
//
///**
// * Маппинг ошибок [[BpmException]] в ошибки [[SpheraException]].
// */
//trait ExceptionMapping {
//  implicit def toSpheraException(exception: Throwable): SpheraException = {
//    exception match {
//      case e: UnknownException =>
//        new SpheraException("bpm2.exceptions.UnknownException")
//
//      case e: EntityNotFoundException =>
//        val code = e.entityType match {
//          case EntityType.DataStructure => "bpm2.exceptions.DataStructureNotFoundException"
//          case EntityType.ProcessTemplate => "bpm2.exceptions.ProcessTemplateNotFoundException"
//          case EntityType.ProjectTemplate => "bpm2.exceptions.ProjectTemplateNotFoundException"
//          case EntityType.FormTemplate => "bpm2.exceptions.FormTemplateNotFoundException"
//          case EntityType.RoleType => "bpm2.exceptions.RoleTypeNotFoundException"
//          case EntityType.FileType => "bpm2.exceptions.FileTypeNotFoundException"
//          case EntityType.CalendarType => "bpm2.exceptions.CalendarTypeNotFoundException"
//          case EntityType.Characteristic => "bpm2.exceptions.CharacteristicNotFoundException"
//          case EntityType.ProcessInfo => "bpm2.exceptions.ProcessInfoNotFoundException"
//          case EntityType.TaskInfo => "bpm2.exceptions.TaskInfoNotFoundException"
//          case EntityType.Verification => "bpm2.exceptions.VerifyException"
//        }
//        new SpheraException(code, Map("entityId" -> e.entityId))
//
//      case e: EntityAlreadyExistsException =>
//        val code = e.entityType match {
//          case EntityType.DataStructure => "bpm2.exceptions.DataStructureAlreadyExistsException"
//          case EntityType.ProcessTemplate => "bpm2.exceptions.ProcessTemplateAlreadyExistsException"
//          case EntityType.ProjectTemplate => "bpm2.exceptions.ProjectTemplateAlreadyExistsException"
//          case EntityType.FormTemplate => "bpm2.exceptions.FormTemplateAlreadyExistsException"
//          case EntityType.RoleType => "bpm2.exceptions.RoleTypeAlreadyExistsException"
//          case EntityType.FileType => "bpm2.exceptions.FileTypeAlreadyExistsException"
//          case EntityType.CalendarType => "bpm2.exceptions.CalendarTypeAlreadyExistsException"
//          case EntityType.Characteristic => "bpm2.exceptions.CharacteristicAlreadyExistsException"
//        }
//        new SpheraException(code, Map("entityId" -> e.entityId))
//
//      case e: DataConsistencyException =>
//        new SpheraException("bpm2.exceptions.DataConsistencyException", Map("path" -> e.path))
//
//      case e: OperationException =>
//        new SpheraException(
//          code = "bpm2.exceptions.OperationException",
//          params = Map(
//            "dateTime" -> e.dateTime.toString,
//            "operationId" -> e.operationId,
//            "operationBpmnId" -> e.operationBpmId,
//            "operationName" -> e.operationName),
//          cause = e.cause.map(toSpheraException))
//
//      case e: UpdateValueException =>
//        new SpheraException(
//          code = "bpm2.exceptions.UpdateValueException",
//          params = Map("dateTime" -> e.dateTime.toString),
//          cause = e.cause.map(toSpheraException))
//
//      case e: VerifyException =>
//        new SpheraException("bpm2.exceptions.VerifyException")
//
//      case e: SpheraException => e
//
//      case e => new SpheraException("bpm2.exceptions.UnknownException")
//    }
//  }
//}