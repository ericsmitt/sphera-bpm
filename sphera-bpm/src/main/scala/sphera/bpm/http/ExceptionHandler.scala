//package sphera.bpm.http
//
//import akka.event.LoggingAdapter
//import akka.http.scaladsl.model.StatusCodes._
//import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse, MediaTypes}
//import akka.http.scaladsl.server.{Directives, ExceptionHandler => AkkaExceptionHandler}
//import sphera.bpm.Exception._
//import sphera.bpm.json._
//import sphera.core.exception.SpheraException
//
//trait ExceptionHandler extends Directives {
//  implicit def exceptionHandler(implicit log: LoggingAdapter): AkkaExceptionHandler = {
//    AkkaExceptionHandler {
//      case e: EntityNotFoundException =>
//        extractUri { uri =>
//          complete {
//            HttpResponse(
//              entity = HttpEntity(
//                contentType = ContentType(MediaTypes.`application/json`),
//                string = JsonError(
//                  httpErrorStatusCode = NotFound.intValue,
//                  httpErrorMessage = NotFound.defaultMessage,
//                  httpReason = NotFound.reason,
//                  error = e).asJson.pretty(printer)),
//              status = NotFound)
//          }
//        }
//      case e: EntityAlreadyExistsException =>
//        extractUri { uri =>
//          complete {
//            HttpResponse(
//              entity = HttpEntity(
//                contentType = ContentType(MediaTypes.`application/json`),
//                string = JsonError(
//                  httpErrorStatusCode = Conflict.intValue,
//                  httpErrorMessage = Conflict.defaultMessage,
//                  httpReason = Conflict.reason,
//                  error = e).asJson.pretty(printer)),
//              status = Conflict)
//          }
//        }
//      case e: DataConsistencyException =>
//        extractUri { uri =>
//          complete {
//            HttpResponse(
//              entity = HttpEntity(
//                contentType = ContentType(MediaTypes.`application/json`),
//                string = JsonError(
//                  httpErrorStatusCode = Conflict.intValue,
//                  httpErrorMessage = Conflict.defaultMessage,
//                  httpReason = Conflict.reason,
//                  error = e).asJson.pretty(printer)),
//              status = Conflict)
//          }
//        }
//      case e: OperationException =>
//        extractUri { uri =>
//          complete {
//            HttpResponse(
//              entity = HttpEntity(
//                contentType = ContentType(MediaTypes.`application/json`),
//                string = JsonError(
//                  httpErrorStatusCode = InternalServerError.intValue,
//                  httpErrorMessage = InternalServerError.defaultMessage,
//                  httpReason = InternalServerError.reason,
//                  error = e).asJson.pretty(printer)),
//              status = InternalServerError)
//          }
//        }
//      case e: UpdateValueException =>
//        extractUri { uri =>
//          complete {
//            HttpResponse(
//              entity = HttpEntity(
//                contentType = ContentType(MediaTypes.`application/json`),
//                string = JsonError(
//                  httpErrorStatusCode = Conflict.intValue,
//                  httpErrorMessage = Conflict.defaultMessage,
//                  httpReason = Conflict.reason,
//                  error = e).asJson.pretty(printer)),
//              status = Conflict)
//          }
//        }
//      case e: CreateProjectException =>
//        extractUri { uri =>
//          complete(
//            HttpResponse(
//              entity = HttpEntity(
//                ContentType(MediaTypes.`application/json`),
//                JsonErrors(
//                  httpErrorStatusCode = NotFound.intValue,
//                  httpErrorMessage = NotFound.defaultMessage,
//                  httpReason = NotFound.reason,
//                  errors = e.x.map(x => toSpheraException(x))).asJson.pretty(printer)),
//              status = NotFound))
//        }
//      case e: CreateProcessException =>
//        extractUri { uri =>
//          complete(
//            HttpResponse(
//              entity = HttpEntity(
//                ContentType(MediaTypes.`application/json`),
//                JsonErrors(
//                  httpErrorStatusCode = NotFound.intValue,
//                  httpErrorMessage = NotFound.defaultMessage,
//                  httpReason = NotFound.reason,
//                  errors = e.x.map(x => toSpheraException(x))).asJson.pretty(printer)),
//              status = NotFound))
//        }
//      case e: VerifyException =>
//        extractUri { uri =>
//          complete {
//            HttpResponse(
//              entity = HttpEntity(
//                contentType = ContentType(MediaTypes.`application/json`),
//                string = JsonError(
//                  httpErrorStatusCode = BadRequest.intValue,
//                  httpErrorMessage = BadRequest.defaultMessage,
//                  httpReason = BadRequest.reason,
//                  error = e).asJson.pretty(printer)),
//              status = BadRequest)
//          }
//        }
//      case e: SpheraException =>
//        extractUri { uri =>
//          complete(
//            HttpResponse(
//              entity = HttpEntity(
//                ContentType(MediaTypes.`application/json`),
//                JsonError(
//                  httpErrorStatusCode = InternalServerError.intValue,
//                  httpErrorMessage = InternalServerError.defaultMessage,
//                  httpReason = InternalServerError.reason,
//                  error = e).asJson.pretty(printer)),
//              status = InternalServerError))
//        }
//      case e: Throwable =>
//        e.printStackTrace()
//
//        complete {
//          val spheraException = new SpheraException(
//            code = "bpm2.exceptions.UnknownException",
//          )
//          HttpResponse(
//            entity = HttpEntity(
//              ContentType(MediaTypes.`application/json`),
//              JsonError(
//                httpErrorStatusCode = InternalServerError.intValue,
//                httpErrorMessage = InternalServerError.defaultMessage,
//                httpReason = InternalServerError.reason,
//                error = spheraException).asJson.pretty(printer)),
//            status = InternalServerError)
//        }
//    }
//  }
//}