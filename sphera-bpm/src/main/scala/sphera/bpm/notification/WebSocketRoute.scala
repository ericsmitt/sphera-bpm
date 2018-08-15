package sphera.bpm.notification

import java.util.UUID

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, PoisonPill }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives.{ handleWebSocketMessages, path, _ }
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Flow, Sink, Source }
import sphera.bpm.notification.actor.WebSocketClientActor
import sphera.core.domain.tenancy.model.User

trait WebSocketRoute {
  implicit def system: ActorSystem
  implicit def notificationManagerActor: ActorRef

  private def newConnection(userId: User.Id): Flow[Message, Message, NotUsed] = {
    val p = WebSocketClientActor.props(userId, notificationManagerActor)
    val a = system.actorOf(p, s"web-socket-client-$userId")

    val in: Sink[Message, NotUsed] =
      Flow[Message].map {
        case TextMessage.Strict(text) => WebSocketClientActor.IncomingMessage(text)
      }.to(Sink.actorRef(a, PoisonPill))

    val out: Source[Message, NotUsed] =
      Source
        .actorRef[WebSocketClientActor.OutgoingMessage](10, OverflowStrategy.dropHead)
        .mapMaterializedValue { x =>
          a ! WebSocketClientActor.Connected(x)
          NotUsed
        }
        .map {
          case WebSocketClientActor.OutgoingMessage(text) => TextMessage(text)
        }

    Flow.fromSinkAndSource(in, out)
  }

  def webSocketRoute: Route = {
    //    (p4("notification") & authenticated) { sessionData =>
    //      handleWebSocketMessages(newConnection(sessionData.userId))
    //    }
    path("notification") {
      handleWebSocketMessages(newConnection(UUID.randomUUID()))
    }
  }
}
