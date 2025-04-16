package io.datatroops

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import io.datatroops.Websocket.AuctionNotifier
import io.datatroops.controllers.{AuctionRoutes, AuctionWebSocketRoutes}
import io.datatroops.entities.{BidTableDAO, ItemTableDAO, UserTableDAO}
import io.datatroops.services.{BidTableService, ItemTableService, UserTableService}
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.sys.addShutdownHook


class AuctionGuardian extends Actor {
  def receive: Receive = {
    case "ping" => sender() ! "pong"
  }
}

object MainApp {
  val config = ConfigFactory.load()
  val db = Database.forConfig("postgres", config)

  def main(args: Array[String]): Unit = {
    println("Real-time auction system is running")

    implicit val system: ActorSystem = ActorSystem("auction-system",config)
    implicit val materializer: Materializer = Materializer(system)

    val auctionNotifier = system.actorOf(AuctionNotifier.props(), "auctionNotifier")

    val itemTableDAO = new ItemTableDAO(db)
    val userTableDAO = new UserTableDAO(db)
    val bidTableDAO = new BidTableDAO(db)

    val itemTableService = new ItemTableService(itemTableDAO)
    val userTableService = new UserTableService(userTableDAO)
    val bidTableService = new BidTableService(bidTableDAO, itemTableDAO,auctionNotifier)

    val auctionWebSocketRoutes = new AuctionWebSocketRoutes(auctionNotifier)

    val auctionRoutes = new AuctionRoutes(
      itemTableService,
      userTableService,
      bidTableService,
      auctionWebSocketRoutes
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(auctionRoutes.route)
    println("Server started at http://localhost:8080")
    println("WebSocket available at ws://localhost:8080/ws/auction")
    system.actorOf(Props[AuctionGuardian], "guardian")

    addShutdownHook {
      println("Shutting down the server...")
      bindingFuture
        .flatMap(_.unbind())
        .onComplete { _ =>
          system.terminate()
          println("Server stopped.")
        }
    }

    try {
      println("Server is running. Press CTRL+C to stop.")
      Thread.currentThread().join()
    } catch {
      case _: InterruptedException =>
        println("Received shutdown signal")
        system.terminate()
    }
  }
}
