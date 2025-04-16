package controllers

import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import io.datatroops.controllers.{AuctionRoutes, AuctionWebSocketRoutes}
import io.datatroops.models.{Bid, Item, User}
import io.datatroops.services.{BidTableService, ItemTableService, UserTableService}

import scala.concurrent.Future
import java.time.LocalDateTime

class AuctionRoutesTest extends AnyWordSpec with Matchers with ScalatestRouteTest with MockitoSugar with ScalaFutures {

  val mockItemTableService: ItemTableService = mock[ItemTableService]
  val mockUserTableService: UserTableService = mock[UserTableService]
  val mockBidTableService: BidTableService = mock[BidTableService]
  val mockAuctionWebSocketRoutes: AuctionWebSocketRoutes = mock[AuctionWebSocketRoutes]

  val routes = new AuctionRoutes(
    mockItemTableService,
    mockUserTableService,
    mockBidTableService,
    mockAuctionWebSocketRoutes
  ).route

  val testItem = Item(1, "Laptop",20000, "Gaming Laptop")
  val testUser = User(1, "John Doe")
  val testBid = Bid(1, 1, 1, 20000.00, Some(LocalDateTime.now()))
  "AuctionRoutes" should {

    "create an item successfully" in {
      when(mockItemTableService.createItem(testItem)).thenReturn(Future.successful(testItem.itemId))

      Post("/item", testItem.toJson) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        responseAs[String] should include("Item created with ID:")
      }
    }

    "create a user successfully" in {
      when(mockUserTableService.createUser(testUser)).thenReturn(Future.successful(testUser.userId))

      Post("/user", testUser.toJson) ~> routes ~> check {
        status shouldBe StatusCodes.Created
        responseAs[String] should include("User created with ID:")
      }
    }

    "place a bid successfully" in {
      when(mockBidTableService.placeBid(testBid)).thenReturn(Future.successful("Bid placed with ID!"))
      Post("/bid", testBid.toJson) ~> routes ~> check {
        status shouldBe StatusCodes.Created

      }
    }

    "fetch all items successfully" in {
      when(mockItemTableService.getAllItems()).thenReturn(Future.successful(Seq(testItem)))

      Get("/items") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("Laptop")
      }
    }

    "fetch all users successfully" in {
      when(mockUserTableService.getAllUsers()).thenReturn(Future.successful(Seq(testUser)))

      Get("/users") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("John Doe")
      }
    }

    "fetch all bids successfully" in {
      when(mockBidTableService.getAllBids()).thenReturn(Future.successful(Seq(testBid)))

      Get("/bids") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("20000")
      }
    }

    "update an item successfully" in {
      when(mockItemTableService.updateItem(1, testItem)).thenReturn(Future.successful(1))

      Put("/item/1", testItem) ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("Item updated successfully.")
      }
    }

    "update a user successfully" in {
      when(mockUserTableService.updateUser(1, testUser)).thenReturn(Future.successful(1))

      Put("/user/1", testUser) ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("User updated successfully.")
      }
    }

    "delete an item successfully" in {
      when(mockItemTableService.deleteItem(1)).thenReturn(Future.successful(1))

      Delete("/item/1") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("Item deleted successfully.")
      }
    }

    "delete a user successfully" in {
      when(mockUserTableService.deleteUser(1)).thenReturn(Future.successful(1))

      Delete("/user/1") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("User deleted successfully.")
      }
    }

    "delete a bid successfully" in {
      when(mockBidTableService.deleteBidsForItem(1)).thenReturn(Future.successful(1))

      Delete("/bid/1") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] should include("Bid deleted successfully.")
      }
    }
  }
}
