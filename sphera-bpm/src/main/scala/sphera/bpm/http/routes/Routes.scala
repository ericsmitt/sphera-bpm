//package sphera.bpm.http.routes
//
//import akka.event.LoggingAdapter
//import akka.http.scaladsl.server.Route
//import akka.util.Timeout
//import sphera.bpm.Bpm
//import sphera.bpm.http.ExceptionHandler
//import sphera.bpm.model.Modifier
//import sphera.core.http.security.SpheraSecurityDirectives.authenticated
//import sphera.core.services.authentication.SessionData
//
//import scala.concurrent.ExecutionContext
//
//trait Routes extends ExceptionHandler
//  with MasterDataRoutes
//  with ProjectRoutes
//  with ProcessRoutes
//  with TaskRoutes
//  with NotificationRoutes {
//  implicit val c: ExecutionContext
//  implicit val t: Timeout
//  implicit val log: LoggingAdapter
//
//  val bpm: Bpm
//
//  def routes: Route = {
//    handleExceptions(exceptionHandler) {
//      authenticated { implicit session: SessionData =>
//        implicit val modifier: Modifier = Modifier(userId = session.userId)
//        pathPrefix("bpm" / "api") {
//          masterDataRoutes ~
//            projectRoutes ~
//            processRoutes ~
//            taskRoutes ~
//            notificationRoutes
//        }
//      }
//    }
//  }
//}
