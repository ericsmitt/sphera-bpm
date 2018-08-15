//defstorage sphera.bpm.jsonstorage
//
//import sphera.bpm.process.Operation
//import io.circe._
//import io.circe.syntax._
//
//trait OperationCodec {
//  implicit val encodeOperation: Encoder[Operation] = new Encoder[Operation] {
//    final def apply(exception: Operation): Json = Json.obj(
//      ("name", exception.name.asJson),
//      ("bpmnId", exception.bpmnId.asJson))
//  }
//
//  implicit val encodeOperations: Encoder[List[Operation]] = new Encoder[List[Operation]] {
//    final def apply(exception: List[Operation]): Json = Json.obj(
//      exception.map(exception => exception.name -> exception.asJson): _*)
//  }
//}
