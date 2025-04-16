package io.datatroops.entities

import io.datatroops.models.Bid

import java.time.LocalDateTime
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class BidTable(tag: Tag) extends Table[Bid](tag, Some("auctions"), "Bid") {
  def bidId = column[Long]("bid_id")
  def userId = column[Long]("user_id")
  def itemId = column[Long]("item_id")
  def bidAmount = column[BigDecimal]("bid_amount")
  def bidTime = column[Option[LocalDateTime]]("bid_time", O.SqlType("TIMESTAMP"))

  def * = (bidId, userId, itemId, bidAmount, bidTime).mapTo[Bid]

  private val itemTable = TableQuery[ItemTable]
  private val userTable = TableQuery[UserTable]

  def itemIdFk = foreignKey("fk_item_id", itemId, itemTable)(_.itemId, onDelete = ForeignKeyAction.Cascade)
  def userIdFk = foreignKey("fk_user_id", userId, userTable)(_.userId, onDelete = ForeignKeyAction.Cascade)
}

class BidTableDAO(db: Database)() {
  private val bids = TableQuery[BidTable]
  def getSchema = bids.schema

  def placeBid(bid: Bid): Future[Long] = {
    val insertQuery = (bids returning bids.map(_.bidId)) += bid
    db.run(insertQuery)
  }

  def getAllBids(): Future[Seq[Bid]] = {
    db.run(bids.result)
  }

  def getBidsForItem(itemId: Long): Future[Seq[Bid]] = {
    db.run(bids.filter(_.itemId === itemId).result)
  }

  def getHighestBidForItem(bidId: Long, itemId: Long): Future[Option[Bid]] = {
    db.run(bids.filter(x => (x.itemId === itemId) && (x.bidId === bidId)).sortBy(_.bidAmount.desc).result.headOption)
  }


  def deleteBidsForItem(itemId: Long): Future[Int] = {
    db.run(bids.filter(_.itemId === itemId).delete)
  }
}
