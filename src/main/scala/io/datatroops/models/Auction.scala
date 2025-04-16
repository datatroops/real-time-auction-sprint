package io.datatroops.models

import spray.json._

case class Auction(bidId: Long,itemId:Long)

object Auction extends DefaultJsonProtocol {
  implicit val auctionFormat: RootJsonFormat[Auction] = jsonFormat2(Auction.apply)
}
