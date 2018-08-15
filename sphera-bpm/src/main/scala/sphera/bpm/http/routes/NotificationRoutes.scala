//package sphera.bpm.http.routes
//
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.server.{ Directives, Route }
//import akka.util.Timeout
//import sphera.bpm.Bpm
//import sphera.bpm.http.model.{ Response, ResponseStatus }
//import sphera.bpm.json.{ JsonNotification, JsonVerification, _ }
//import sphera.bpm.model.Modifier
//import sphera.bpm.notification.model.{ MailNotification, SmsNotification }
//import sphera.core.utils.Generator
//
//import scala.concurrent.ExecutionContext
//
//trait NotificationRoutes extends Directives with Generator {
//  implicit val c: ExecutionContext
//  implicit val t: Timeout
//
//  val bpm: Bpm
//
//  def notificationRoutes(implicit modifier: Modifier): Route = {
//    /**
//     * При сбросе сообщения [[SmsNotification.Verification]] автоматически создается верификация.
//     */
//    val push = {
//      (post & pathPrefix("notifications") & entity(as[JsonNotification]) & pathEndOrSingleSlash) { x =>
//        complete {
//          val (Some(field), index) = Seq(x.email, x.phone)
//            .zipWithIndex
//            .filter(_._1.nonEmpty)
//            .head
//
//          val notification = index match {
//            case 0 => MailNotification.TextMessage(
//              id = generateUUID,
//              email = field,
//              subject = x.subject,
//              message = x.message)
//            case 1 => SmsNotification.TextMessage(
//              id = generateUUID,
//              phone = field,
//              subject = x.subject,
//              message = x.message)
//          }
//
//          bpm.push(notification)
//
//          Accepted -> Response(
//            entityId = notification.id.toString,
//            status = ResponseStatus.Pending)
//        }
//      } ~
//        (post & pathPrefix("verifications") & entity(as[JsonVerification]) & pathEndOrSingleSlash) { x =>
//          complete {
//            val notification = SmsNotification.Verification(
//              id = generateUUID,
//              phone = x.phone,
//              subject = x.subject,
//              message = x.message,
//              code = x.code)
//
//            bpm.push(notification)
//
//            Accepted -> Response(
//              entityId = notification.id.toString,
//              status = ResponseStatus.Pending)
//          }
//        }
//    }
//
//    val verify = {
//      (post & pathPrefix("verifications" / JavaUUID) & entity(as[String]) & pathEndOrSingleSlash) { (x, y) =>
//        complete(bpm.verify(x, y).map(_.asJsonResponse))
//      }
//    }
//
//    push ~ verify
//  }
//}