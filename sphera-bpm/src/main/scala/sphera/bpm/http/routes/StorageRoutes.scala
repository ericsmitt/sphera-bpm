//package sphera.bpm.http.routes
//
//import akka.actor.ActorRef
//import akka.http.scaladsl.server.{ Directives, Route }
//import akka.util.Timeout
//import sphera.bpm.Bpm
//import sphera.bpm.actor.ActorId
//import sphera.bpm.definition.{ AnyDefinition, Data, DataElement }
//import sphera.bpm.json._
//import sphera.bpm.model.Modifier
//
//import scala.concurrent.{ ExecutionContext, Future }
//
//trait StorageRoutes extends Directives {
//  implicit val c: ExecutionContext
//  implicit val t: Timeout
//
//  val bpm: Bpm
//
//  def storageRoutes(storage: ActorRef)(implicit modifier: Modifier): Route = {
//    def dataRoutes(nodeId: ActorId.Id) = {
//      val getData = {
//        (get & pathEndOrSingleSlash) {
//          complete(bpm.getData(storage, nodeId).map(_.asJson))
//        }
//      }
//      val getDataElement = {
//        (get & pathPrefix(Segment) & pathEndOrSingleSlash) { path =>
//          complete {
//            val x: Future[DataElement] = bpm.getDataElement(storage, nodeId, path)
//            x.map { x =>
//              println(x)
//              x.toString
//              x.asJson
//            }
//          }
//        }
//      }
//      val updateData = {
//        (put & entity(as[Data]) & pathEndOrSingleSlash) { x =>
//          complete(bpm.updateData(storage, nodeId, x).map(_.asJsonResponse))
//        }
//      }
//      val updateDataElement = {
//        (put & entity(as[DataElement]) & pathEndOrSingleSlash) { x =>
//          complete(bpm.updateDataElement(storage, nodeId, x).map(_.asJsonResponse))
//        }
//      }
//
//      pathPrefix("data") {
//        getData ~
//          getDataElement ~
//          updateData ~
//          updateDataElement
//      }
//    }
//
//    def definitionRoutes(nodeId: ActorId.Id) = {
//      val getDefinitions = {
//        (get & pathEndOrSingleSlash) {
//          complete(bpm.getDefinitions(storage, nodeId).map(_.asJson))
//        }
//      }
//      val getDefinition = {
//        (get & pathPrefix(Segment) & pathEndOrSingleSlash) { path =>
//          complete {
//            val x: Future[AnyDefinition] = bpm.getDefinition(storage, nodeId, path)
//            x.map { x =>
//              println(x)
//              x.toString
//              x.asJson
//            }
//          }
//        }
//      }
//
//      pathPrefix("defStorage") {
//        getDefinitions ~
//          getDefinition
//      }
//    }
//
//    pathPrefix(Segment) { nodeId =>
//      dataRoutes(nodeId) ~
//        definitionRoutes(nodeId)
//    }
//  }
//}
