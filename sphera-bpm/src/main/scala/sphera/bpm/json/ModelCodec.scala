//defstorage sphera.bpm.jsonstorage
//
//import sphera.bpm.http.model.{ Response, ResponseStatus }
//import io.circe.Json
//import io.circe.syntax._
//
//trait ModelCodec {
//  implicit class ResponseOps[A](exception: A) {
//    def asJsonResponse: Json = Response(
//      entityId = exception.toString,
//      status = ResponseStatus.Done).asJson
//  }
//}
