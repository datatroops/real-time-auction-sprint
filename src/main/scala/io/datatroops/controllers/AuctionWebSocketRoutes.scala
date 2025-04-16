package io.datatroops.controllers

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import io.datatroops.Websocket.{Subscribe, Unsubscribe}

import scala.concurrent.ExecutionContext

class AuctionWebSocketRoutes(auctionNotifier: ActorRef)
                            (implicit system: ActorSystem, ec: ExecutionContext, mat: Materializer) {

  def websocketFlow(): Flow[Message, Message, Any] = {
    Flow.fromSinkAndSourceMat(
      Sink.ignore,
      Source.actorRef[TextMessage](bufferSize = 10, OverflowStrategy.dropHead)
    ) { (sinkMat, clientRef) =>
      auctionNotifier ! Subscribe(clientRef)
      system.log.info("WebSocket client subscribed.")

      sinkMat.onComplete(_ => auctionNotifier ! Unsubscribe(clientRef))

      clientRef
    }
  }

  def route: Route =
    path("ws" / "auction") {
      handleWebSocketMessages(websocketFlow())
    }
}
