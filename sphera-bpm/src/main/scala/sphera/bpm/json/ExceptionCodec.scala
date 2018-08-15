//defstorage sphera.bpm.jsonstorage
//
//import sphera.bpm.Exception.BpmException
//import sphera.core.exception.SpheraException
//import io.circe._
//import io.circe.syntax._
//
//trait ExceptionCodec {
//  implicit val encodeSpheraException: Encoder[SpheraException] = new Encoder[SpheraException] {
//    final def apply(exception: SpheraException): Json = {
//      val cause: Option[(String, Json)] = Option(exception.getCause).map { throwable =>
//        "cause" -> toSpheraException(throwable).asJson
//      }
//
//      val params: Seq[(String, Json)] =
//        exception.exceptionMessage
//          .map { case (k, v) => (k, v.asJson) }
//          .toSeq
//
//      val fields = cause
//        .map(params :+ _)
//        .getOrElse(params)
//
//      Json.obj(fields: _*)
//    }
//  }
//
//  implicit val encodeBpmException: Encoder[BpmException] = new Encoder[BpmException] {
//    import sphera.bpm.Exception._
//    final def apply(exception: BpmException): Json = toSpheraException(exception).asJson
//  }
//}
