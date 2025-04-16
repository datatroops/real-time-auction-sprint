import akka.actor.ActorSystem
import akka.testkit.TestProbe
import io.datatroops.entities.{BidTableDAO, ItemTableDAO}
import io.datatroops.models.{Auction, Bid, Item}
import io.datatroops.services.BidTableService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future
import java.time.LocalDateTime

class BidTableServiceTest extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val system: ActorSystem = ActorSystem("TestSystem")
  val mockBidTableDAO: BidTableDAO = mock[BidTableDAO]
  val mockItemTableDAO: ItemTableDAO = mock[ItemTableDAO]
  val auctionNotifier = TestProbe()

  val bidTableService = new BidTableService(mockBidTableDAO, mockItemTableDAO, auctionNotifier.ref)

  val testBid = Bid(1, 1, 1, 20000.0, Some(LocalDateTime.now()))

    "BidTableService" should {
      "create an auction successfully" in {
        when(mockItemTableDAO.getItemById(any[Long]))
          .thenReturn(Future.successful(Some(Item(1, "Laptop", 15000.0, "qwerty"))))

        val auction = Auction(1, 1)
        bidTableService.createAuction(auction).map { result =>
          result should include("Auction created with Bid ID: 1")
        }
      }

    "place a bid successfully" in {
      when(mockItemTableDAO.getItemById(any[Long]))
        .thenReturn(Future.successful(Some(Item(1, "Laptop", 15000.0,"qwerty"))))
      when(mockBidTableDAO.getHighestBidForItem(any[Long], any[Long]))
        .thenReturn(Future.successful(None))
      when(mockBidTableDAO.placeBid(any[Bid]))
        .thenReturn(Future.successful(1))

      bidTableService.placeBid(testBid).map { result =>
        result should include("Bid accepted!")
      }
    }

    "reject a bid lower than the starting price" in {
      when(mockItemTableDAO.getItemById(any[Long]))
        .thenReturn(Future.successful(Some(Item(1, "Laptop", 15000.0,"qwerty"))))
      when(mockBidTableDAO.getHighestBidForItem(any[Long], any[Long]))
        .thenReturn(Future.successful(None))

      val lowBid = testBid.copy(bidAmount = 10000.0)

      bidTableService.placeBid(lowBid).map { result =>
        result should include("Bid rejected! Your bid must be higher than the starting price")
      }
    }

    "get all bids" in {
      when(mockBidTableDAO.getAllBids())
        .thenReturn(Future.successful(Seq(testBid)))

      bidTableService.getAllBids().map { result =>
        result should have size 1
      }
    }

    "get bids for an item" in {
      when(mockBidTableDAO.getBidsForItem(any[Long]))
        .thenReturn(Future.successful(Seq(testBid)))

      bidTableService.getBidsForItem(1).map { result =>
        result should have size 1
      }
    }

    "get the highest bid for an item" in {
      when(mockBidTableDAO.getHighestBidForItem(any[Long], any[Long]))
        .thenReturn(Future.successful(Some(testBid)))

      bidTableService.getHighestBidForItem(1, 1).map { result =>
        result shouldBe Some(testBid)
      }
    }

    "delete bids for an item" in {
      when(mockBidTableDAO.deleteBidsForItem(any[Long]))
        .thenReturn(Future.successful(1))

      bidTableService.deleteBidsForItem(1).map { result =>
        result shouldBe 1
      }
    }
  }
}
