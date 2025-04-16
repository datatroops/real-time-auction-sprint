package entities

import io.datatroops.entities.{BidTableDAO, ItemTableDAO, UserTableDAO}
import io.datatroops.models.{Bid, Item, User}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class BidTableDAOTest extends AsyncFlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  val db = Database.forConfig("testdb")
  val bidTableDAO = new BidTableDAO(db)
  val userTableDAO = new UserTableDAO(db)
  val itemTableDAO = new ItemTableDAO(db)

  override def beforeAll(): Unit = {
    val setup = DBIO.seq(
      bidTableDAO.getSchema.createIfNotExists,
      userTableDAO.getSchema.createIfNotExists,
      itemTableDAO.getSchema.createIfNotExists
    )
    val user = User(1, "Lakshay-test")
    val item = Item(2, "gold chain", 490, "test-desc")
    itemTableDAO.createItem(item)
    userTableDAO.createUser(user)
    Await.result(db.run(setup), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.shutdown, Duration.Inf)
  }

  "BidTableDAO" should "place a bid and return the bid ID" in {
    val bid = Bid(1, 1, 2, BigDecimal(500), Some(LocalDateTime.now()))
    bidTableDAO.placeBid(bid).map { bidId =>
      bidId should be > 0L
    }
  }

  it should "retrieve all bids" in {
    bidTableDAO.getAllBids().map { bids =>
      bids should not be empty
    }
  }

  it should "retrieve bids for a specific item" in {
    val itemId = 2L
    bidTableDAO.getBidsForItem(itemId).map { bids =>
      bids should not be empty
      all(bids.map(_.itemId)) shouldBe itemId
    }
  }


  it should "return the highest bid for an item" in {
    val itemId = 2L
    val bidId = 1L
    bidTableDAO.getHighestBidForItem(bidId, itemId).map { highestBid =>
      highestBid shouldBe defined
      highestBid.get.bidAmount should be >= BigDecimal(500)
    }
  }

  it should "delete bids for an item" in {
    val itemId = 2L
    bidTableDAO.deleteBidsForItem(itemId).map { deletedCount =>
      deletedCount should be >= 0
    }
  }
}
