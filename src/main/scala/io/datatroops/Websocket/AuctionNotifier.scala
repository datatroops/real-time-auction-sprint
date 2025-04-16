package io.datatroops.Websocket

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.model.ws.TextMessage

case class Subscribe(client: ActorRef)
case class Unsubscribe(client: ActorRef)
case class BroadcastMessage(message: String)

class AuctionNotifier extends Actor {
  private var clients: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case Subscribe(client) =>
      clients += client
      context.system.log.info(s"Client subscribed. Total clients: ${clients.size}")

    case Unsubscribe(client) =>
      clients -= client
      context.system.log.info(s"Client unsubscribed. Total clients: ${clients.size}")

    case BroadcastMessage(message) =>
      context.system.log.info(s"Broadcasting message to ${clients.size} clients: $message")
      clients.foreach { client =>
        client ! TextMessage(message)
      }
  }
}
object AuctionNotifier {
  def props(): Props = Props[AuctionNotifier]
}
