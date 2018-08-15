//package sphera.bpm.http.routes
//
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{ Directives, Route }
//import akka.util.Timeout
//import sphera.bpm.Bpm
//import sphera.bpm.json._
//import sphera.bpm.model.Modifier
//import sphera.bpm.project.{ CreateProjectInfo, UpdateProjectInfo }
//
//import scala.concurrent.ExecutionContext
//
//trait ProjectRoutes extends Directives with StorageRoutes {
//  implicit val c: ExecutionContext
//  implicit val t: Timeout
//
//  val bpm: Bpm
//
//  def projectRoutes(implicit modifier: Modifier): Route = {
//    val getProjectInfoAll = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getProjectInfoAllAsSet.map(_.asJson))
//      }
//    }
//    val getProjectInfo = {
//      (get & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProjectInfo(x).map(_.asJson))
//      }
//    }
//    val createProject = {
//      (post & entity(as[CreateProjectInfo]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createProject(x, modifier).map(_.asJson))
//      }
//    }
//    val updateProject = {
//      (put & entity(as[UpdateProjectInfo]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateProject(x, modifier).map(_.asJsonResponse))
//      }
//    }
//    val deleteProject = {
//      (delete & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteProject(x, modifier).map(_.asJsonResponse))
//      }
//    }
//    val getProjectExtendedInfo = {
//      (get & pathPrefix(Segment / "extendedInfo") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProjectExtendedInfo(x).map(_.asJson))
//      }
//    }
//    val getProjectStatus = {
//      (get & pathPrefix(Segment / "status") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProjectStatus(x).map(_.asJson))
//      }
//    }
//    val startProject = {
//      (post & pathPrefix(Segment / "start") & pathEndOrSingleSlash) { x =>
//        complete(bpm.startProject(x, modifier).map(_.asJsonResponse))
//      }
//    }
//    val completeProject = {
//      (post & pathPrefix(Segment / "complete") & pathEndOrSingleSlash) { x =>
//        complete(bpm.completeProject(x, modifier).map(_.asJsonResponse))
//      }
//    }
//    val getActiveOperations = {
//      (get & pathPrefix(Segment / "activeProcesses") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getActiveProcesses(x).map(_.asJson))
//      }
//    }
//
//    def projectHistoryDataRoutes = {
//      def getProjectHistoryDataAll = {
//        (get & pathEndOrSingleSlash) {
//          complete(bpm.getProjectHistoryDataAll.map(_.asJson))
//        }
//      }
//      def getProjectHistoryData = {
//        (get & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//          complete(bpm.getProjectHistoryData(x).map(_.asJson))
//        }
//      }
//
//      pathPrefix("history") {
//        getProjectHistoryDataAll ~
//          getProjectHistoryData
//      }
//    }
//
//    pathPrefix("projects") {
//      getProjectInfoAll ~
//        projectHistoryDataRoutes ~
//        getProjectInfo ~
//        createProject ~
//        updateProject ~
//        deleteProject ~
//        getProjectExtendedInfo ~
//        getProjectStatus ~
//        startProject ~
//        completeProject ~
//        getActiveOperations ~
//        storageRoutes(bpm.projectManagerActor)
//    }
//  }
//}