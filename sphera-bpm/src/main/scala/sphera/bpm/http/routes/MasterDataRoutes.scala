//package sphera.bpm.http.routes
//
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{ Directives, Route }
//import akka.util.Timeout
//import sphera.bpm.Bpm
//import sphera.bpm.json._
//import sphera.bpm.masterdata.model._
//import sphera.bpm.model.Modifier
//
//import scala.concurrent.ExecutionContext
//
//trait MasterDataRoutes extends Directives {
//  implicit val c: ExecutionContext
//  implicit val t: Timeout
//
//  val bpm: Bpm
//
//  def dataStructures(implicit modifier: Modifier): Route = {
//    val getDataStructures = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getDataStructuresAsSet.map(_.asJson))
//      }
//    }
//    val getDataStructure = {
//      (get & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getDataStructure(x).map(_.asJson))
//      }
//    }
//    val createDataStructure = {
//      (post & entity(as[UpdateDataStructure]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createDataStructure(x).map(_.asJsonResponse))
//      }
//    }
//    val updateDataStructure = {
//      (put & entity(as[UpdateDataStructure]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateDataStructure(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteDataStructure = {
//      (delete & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteDataStructure(x).map(_.asJsonResponse))
//      }
//    }
//    pathPrefix("dss") {
//      getDataStructures ~
//        getDataStructure ~
//        createDataStructure ~
//        updateDataStructure ~
//        deleteDataStructure
//    }
//  }
//
//  def projectTemplates(implicit modifier: Modifier): Route = {
//    val getProjectTemplates = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getProjectTemplatesAsSet.map(_.asJson))
//      }
//    }
//    val getProjectTemplate = {
//      (get & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProjectTemplate(x).map(_.asJson))
//      }
//    }
//    val createProjectTemplate = {
//      (post & entity(as[UpdateProjectTemplate]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createProjectTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    val updateProjectTemplate = {
//      (put & entity(as[UpdateProjectTemplate]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateProjectTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteProjectTemplate = {
//      (delete & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteProjectTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    pathPrefix("projectTemplates") {
//      getProjectTemplates ~
//        getProjectTemplate ~
//        createProjectTemplate ~
//        updateProjectTemplate ~
//        deleteProjectTemplate
//    }
//  }
//
//  def processTemplates(implicit modifier: Modifier): Route = {
//    val getProcessTemplates = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getProcessTemplatesAsSet.map(_.asJson))
//      }
//    }
//    val getProcessTemplate = {
//      (get & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProcessTemplate(x).map(_.asJson))
//      }
//    }
//    val createProcessTemplate = {
//      (post & entity(as[UpdateProcessTemplate]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createProcessTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    val updateProcessTemplate = {
//      (put & entity(as[UpdateProcessTemplate]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateProcessTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteProcessTemplate = {
//      (delete & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteProcessTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    pathPrefix("processTemplates") {
//      getProcessTemplates ~
//        getProcessTemplate ~
//        createProcessTemplate ~
//        updateProcessTemplate ~
//        deleteProcessTemplate
//    }
//  }
//
//  def formTemplates(implicit modifier: Modifier): Route = {
//    val getFormTemplates = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getFormTemplatesAsSet.map(_.asJson))
//      }
//    }
//    val getFormTemplate = {
//      (get & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getFormTemplate(x).map(_.asJson))
//      }
//    }
//    val createFormTemplate = {
//      (post & entity(as[UpdateFormTemplate]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createFormTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    val updateFormTemplate = {
//      (put & entity(as[UpdateFormTemplate]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateFormTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteFormTemplate = {
//      (delete & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteFormTemplate(x).map(_.asJsonResponse))
//      }
//    }
//    pathPrefix("formTemplates") {
//      getFormTemplates ~
//        getFormTemplate ~
//        createFormTemplate ~
//        updateFormTemplate ~
//        deleteFormTemplate
//    }
//  }
//
//  def roleTypes(implicit modifier: Modifier): Route = {
//    val getRoleTypes = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getRoleTypesAsSet.map(_.asJson))
//      }
//    }
//    val getRoleType = {
//      (get & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getRoleType(x).map(_.asJson))
//      }
//    }
//    val createRoleType = {
//      (post & entity(as[UpdateRoleType]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createRoleType(x).map(_.asJsonResponse))
//      }
//    }
//    val updateRoleType = {
//      (put & entity(as[UpdateRoleType]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateRoleType(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteRoleType = {
//      (delete & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteRoleType(x).map(_.asJsonResponse))
//      }
//    }
//    pathPrefix("roleTypes") {
//      getRoleTypes ~
//        getRoleType ~
//        createRoleType ~
//        updateRoleType ~
//        deleteRoleType
//    }
//  }
//
//  def fileTypes(implicit modifier: Modifier): Route = {
//    val getFileTypes = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getFileTypesAsSet.map(_.asJson))
//      }
//    }
//    val getFileType = {
//      (get & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getFileType(x).map(_.asJson))
//      }
//    }
//    val createFileType = {
//      (post & entity(as[UpdateFileType]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createFileType(x).map(_.asJsonResponse))
//      }
//    }
//    val updateFileType = {
//      (put & entity(as[UpdateFileType]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateFileType(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteFileType = {
//      (delete & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteFileType(x).map(_.asJsonResponse))
//      }
//    }
//    pathPrefix("fileTypes") {
//      getFileTypes ~
//        getFileType ~
//        createFileType ~
//        updateFileType ~
//        deleteFileType
//    }
//  }
//
//  def characteristics(implicit modifier: Modifier): Route = {
//    val getCharacteristics = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getCharacteristicsAsSet.map(_.asJson))
//      }
//    }
//    val getCharacteristic = {
//      (get & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getCharacteristic(x).map(_.asJson))
//      }
//    }
//    val createCharacteristic = {
//      (post & entity(as[UpdateCharacteristic]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createCharacteristic(x).map(_.asJsonResponse))
//      }
//    }
//    val updateCharacteristic = {
//      (put & entity(as[UpdateCharacteristic]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateCharacteristic(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteCharacteristic = {
//      (delete & pathPrefix(JavaUUID) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteCharacteristic(x).map(_.asJsonResponse))
//      }
//    }
//    pathPrefix("characteristics") {
//      getCharacteristics ~
//        getCharacteristic ~
//        createCharacteristic ~
//        updateCharacteristic ~
//        deleteCharacteristic
//    }
//  }
//
//  def masterDataRoutes(implicit modifier: Modifier): Route = {
//    pathPrefix("masterData") {
//      dataStructures ~
//        projectTemplates ~
//        processTemplates ~
//        formTemplates ~
//        roleTypes ~
//        fileTypes ~
//        characteristics
//    }
//  }
//}