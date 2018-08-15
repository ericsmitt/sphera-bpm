//package sphera.bpm.http.routes
//
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{ Directives, Route }
//import akka.util.Timeout
//import sphera.bpm.Bpm
//import sphera.bpm.json._
//import sphera.bpm.model.Modifier
//import sphera.bpm.process.{ CreateProcessInfo, UpdateProcessInfo }
//
//import scala.concurrent.ExecutionContext
//
//trait ProcessRoutes extends Directives with StorageRoutes {
//  implicit val c: ExecutionContext
//  implicit val t: Timeout
//
//  val bpm: Bpm
//
//  def processRoutes(implicit modifier: Modifier): Route = {
//    val getProcessInfoAll = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getProcessInfoSet.map(_.asJson))
//      }
//    }
//    val getProcessInfo = {
//      (get & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProcessInfo(x).map(_.asJson))
//      }
//    }
//    val createProcess = {
//      (post & entity(as[CreateProcessInfo]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createProcess(x).map(_.asJson))
//      }
//    }
//    val updateProcess = {
//      (put & entity(as[UpdateProcessInfo]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateProcess(x).map(_.asJsonResponse))
//      }
//    }
//    val deleteProcess = {
//      (delete & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteProcess(x).map(_.asJsonResponse))
//      }
//    }
//    val getProcessExtendedInfo = {
//      (get & pathPrefix(Segment / "extendedInfo") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProcessExtendedInfo(x).map(_.asJson))
//      }
//    }
//    val getProcessStatus = {
//      (get & pathPrefix(Segment / "status") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getProcessStatus(x).map(_.asJson))
//      }
//    }
//    val startProcess = {
//      (post & pathPrefix(Segment / "start") & pathEndOrSingleSlash) { x =>
//        complete(bpm.startProcess(x).map(_.asJsonResponse))
//      }
//    }
//    val completeProcess = {
//      (post & pathPrefix(Segment / "complete") & pathEndOrSingleSlash) { x =>
//        complete(bpm.completeProcess(x).map(_.asJsonResponse))
//      }
//    }
//    val getActiveOperations = {
//      (get & pathPrefix(Segment / "activeOperations") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getActiveOperations(x).map(_.asJson))
//      }
//    }
//
//    def processHistoryDataRoutes = {
//      def getProcessHistoryDataAll = {
//        (get & pathEndOrSingleSlash) {
//          complete(bpm.getProcessHistoryDataAll.map(_.asJson))
//        }
//      }
//      def getProcessHistoryData = {
//        (get & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//          complete(bpm.getProcessHistoryData(x).map(_.asJson))
//        }
//      }
//
//      pathPrefix("history") {
//        getProcessHistoryDataAll ~
//          getProcessHistoryData
//      }
//    }
//
//    pathPrefix("processes") {
//      getProcessInfoAll ~
//        processHistoryDataRoutes ~
//        getProcessInfo ~
//        createProcess ~
//        updateProcess ~
//        deleteProcess ~
//        getProcessExtendedInfo ~
//        getProcessStatus ~
//        startProcess ~
//        completeProcess ~
//        getActiveOperations ~
//        storageRoutes(bpm.processManagerActor)
//    }
//  }
//}