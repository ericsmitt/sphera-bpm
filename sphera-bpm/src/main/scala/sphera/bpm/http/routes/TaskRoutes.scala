//package sphera.bpm.http.routes
//
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{ Directives, Route }
//import akka.util.Timeout
//import sphera.bpm.Bpm
//import sphera.bpm.json._
//import sphera.bpm.model.Modifier
//import sphera.bpm.task.{ CreateTaskInfo, UpdateTaskInfo }
//import sphera.bpm.task.model._
//
//import scala.concurrent.ExecutionContext
//
//trait TaskRoutes extends Directives with StorageRoutes {
//  implicit val c: ExecutionContext
//  implicit val t: Timeout
//
//  val bpm: Bpm
//
//  def taskRoutes(implicit modifier: Modifier): Route = {
//    val getTaskInfoAll = {
//      (get & pathEndOrSingleSlash) {
//        complete(bpm.getTaskInfoAllAsSet.map(_.asJson))
//      }
//    }
//    val getTaskInfo = {
//      (get & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//        complete(bpm.getTaskInfo(x).map(_.asJson))
//      }
//    }
//    val createTask = {
//      (post & entity(as[CreateTaskInfo]) & pathEndOrSingleSlash) { x =>
//        complete(Created -> bpm.createTask(x, modifier).map(_.asJson))
//      }
//    }
//    val updateTask = {
//      (put & entity(as[UpdateTaskInfo]) & pathEndOrSingleSlash) { x =>
//        complete(bpm.updateTask(x, modifier).map(_.asJsonResponse))
//      }
//    }
//    val deleteTask = {
//      (delete & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//        complete(bpm.deleteTask(x, modifier).map(_.asJsonResponse))
//      }
//    }
//    val getTaskExtendedInfo = {
//      (get & pathPrefix(Segment / "extendedInfo") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getTaskExtendedInfo(x).map(_.asJson))
//      }
//    }
//    val getTaskStatus = {
//      (get & pathPrefix(Segment / "status") & pathEndOrSingleSlash) { x =>
//        complete(bpm.getTaskStatus(x).map(_.asJson))
//      }
//    }
//    val startTask = {
//      (post & pathPrefix(Segment / "start") & pathEndOrSingleSlash) { x =>
//        complete(bpm.startTask(x, modifier).map(_.asJsonResponse))
//      }
//    }
//    val completeTask = {
//      (post & pathPrefix(Segment / "complete") & pathEndOrSingleSlash) { x =>
//        complete(bpm.completeTask(x, modifier).map(_.asJsonResponse))
//      }
//    }
//
//    def taskHistoryDataRoutes = {
//      def getTaskHistoryDataAll = {
//        (get & pathEndOrSingleSlash) {
//          complete(bpm.getTaskHistoryDataAll.map(_.asJson))
//        }
//      }
//      def getTaskHistoryData = {
//        (get & pathPrefix(Segment) & pathEndOrSingleSlash) { x =>
//          complete(bpm.getTaskHistoryData(x).map(_.asJson))
//        }
//      }
//
//      pathPrefix("history") {
//        getTaskHistoryDataAll ~
//          getTaskHistoryData
//      }
//    }
//
//    pathPrefix("tasks") {
//      getTaskInfoAll ~
//        taskHistoryDataRoutes ~
//        getTaskInfo ~
//        createTask ~
//        updateTask ~
//        deleteTask ~
//        getTaskExtendedInfo ~
//        getTaskStatus ~
//        startTask ~
//        completeTask ~
//        storageRoutes(bpm.taskManagerActor)
//    }
//  }
//}
