package io.datatroops.services

import akka.actor.{ActorRef, ActorSystem, Cancellable}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.stream.Materializer
import io.datatroops.Websocket.BroadcastMessage
import io.datatroops.entities.{BidTableDAO, ItemTableDAO}
import io.datatroops.models.{Auction, Bid}
import spray.json._

import java.time.LocalDateTime
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class BidTableService(bidTableDAO: BidTableDAO, itemTableDAO: ItemTableDAO, auctionNotifier: ActorRef)
                     (implicit ec: ExecutionContext, system: ActorSystem, mat: Materializer) {

  private var auctionTimers: Map[Long, Cancellable] = Map.empty
  private var activeAuctions: Set[Long] = Set.empty

  def createAuction(auction:Auction): Future[String] = {
    if (activeAuctions.contains(auction.bidId)) {
      Future.successful(s"Auction already exists for bid ID: ${auction.bidId}")
    } else {
      itemTableDAO.getItemById(auction.itemId).flatMap {
        case Some(item) =>
          activeAuctions += auction.bidId
          startAuctionTimer(auction.bidId,auction.itemId)
          Future.successful(s"Auction created with Bid ID: ${auction.bidId} for item '${item.name}' with starting price: ${item.starting_price}")

        case None =>
          Future.successful(s"Item with ID ${auction.itemId} not found")
      }
    }
  }



  def placeBid(bid: Bid): Future[String] = {
    itemTableDAO.getItemById(bid.itemId).flatMap {
      case Some(item) if activeAuctions.contains(bid.bidId) =>
        getHighestBidForItem(bid.bidId, bid.itemId).flatMap {
          case None =>
            if (bid.bidAmount < item.starting_price) {
              Future.successful(s"Bid rejected! Your bid must be higher than the starting price of ${item.starting_price}")
            } else {
              placeNewBid(bid)
            }

          case Some(highestBid) =>
            if (bid.bidAmount <= highestBid.bidAmount) {
              Future.successful(s"Bid rejected! Your bid must be higher than ${highestBid.bidAmount}")
            } else {
              placeNewBid(bid)
            }
        }

      case Some(_) =>
        Future.successful(s"Auction with item ID ${bid.itemId} does not exist")

      case None =>
        Future.successful(s"Item with ID ${bid.itemId} not found")
    }
  }

  private def placeNewBid(bid: Bid): Future[String] = {
    val newBid = bid.copy(bidTime = Some(LocalDateTime.now()))
    bidTableDAO.placeBid(newBid).map { _ =>
      val bidMessage = s"Bid accepted! New highest bid: ${newBid.bidAmount}"
      auctionNotifier ! BroadcastMessage(newBid.toJson.toString())
      bidMessage
    }
  }

  protected def startAuctionTimer(bidId: Long, itemId: Long): Unit = {
    val startMessage = s"Auction started for Bid ID: $bidId, Item ID: $itemId"
    auctionNotifier ! BroadcastMessage(startMessage)

    val cancellable = system.scheduler.scheduleOnce(3.minutes) {
      endAuction(bidId, itemId)
    }

    auctionTimers += (bidId -> cancellable)
    system.log.info(startMessage)
  }

  def endAuction(bidId: Long, itemId: Long): Unit = {
    getHighestBidForItem(bidId, itemId).foreach {
      case Some(winningBid) =>
        val winnerMessage = s"Auction ended! Winning bid: ${winningBid.bidAmount} by user ${winningBid.userId}"
        auctionNotifier ! BroadcastMessage(winnerMessage)
        sendAuctionWebhook(winningBid)

      case None =>
        auctionNotifier ! BroadcastMessage(s"Auction with Bid ID: $bidId, Item ID: $itemId ended with no bids.")
    }

    auctionTimers -= bidId
    activeAuctions -= bidId
    system.log.info(s"Auction ended for Bid ID: $bidId, Item ID: $itemId")
  }


  def sendAuctionWebhook(winningBid: Bid): Unit = {
    val webhookUrl = "https://4cfca2e1-9b0e-4354-a240-0d21682ec01c.mock.pstmn.io"
    val jsonPayload = winningBid.toJson.toString()

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = webhookUrl,
      entity = HttpEntity(ContentTypes.`application/json`, jsonPayload)
    )

    Http().singleRequest(request).onComplete {
      case Success(response) =>
        response.entity.dataBytes.runFold("")(_ + _.utf8String).onComplete {
          case Success(body) =>
             system.log.info(s"Webhook sent successfully. Status: ${response.status}, Body: $body")
            response.discardEntityBytes()
          case Failure(ex) =>
            system.log.error(s"Failed to read response body: ${ex.getMessage}")
        }

      case Failure(exception) =>
        system.log.error(s"Failed to send webhook: ${exception.getMessage}")
    }
  }

  def getAllBids(): Future[Seq[Bid]] = bidTableDAO.getAllBids()

  def getBidsForItem(bidId: Long): Future[Seq[Bid]] = bidTableDAO.getBidsForItem(bidId)

  def getHighestBidForItem(bidId: Long, itemId: Long): Future[Option[Bid]] = bidTableDAO.getHighestBidForItem(bidId, itemId)

  def deleteBidsForItem(bidId: Long): Future[Int] = bidTableDAO.deleteBidsForItem(bidId)
}
