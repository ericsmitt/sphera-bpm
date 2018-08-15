//package sphera.bpm.http.model
//
//case class Response(entityId: String, status: ResponseStatus)
//
//sealed trait ResponseStatus
//
//object ResponseStatus {
//  case object Pending extends CqrsResponseStatus
//  case object Running extends CqrsResponseStatus
//  case object Done extends CqrsResponseStatus
//}