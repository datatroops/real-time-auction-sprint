package io.datatroops.controllers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import io.datatroops.models.{Auction, Bid, Item, User}
import io.datatroops.services.{BidTableService, ItemTableService, UserTableService}
import spray.json._

import scala.util.{Failure, Success, Try}

class AuctionRoutes(
                     itemTableService: ItemTableService,
                     userTableService: UserTableService,
                     bidTableService: BidTableService,
                     auctionWebSocketRoutes: AuctionWebSocketRoutes
                   ) {

  def createItemRoute: Route =
    path("item") {
      post {
        entity (as[Item]) { item =>
          onComplete(itemTableService.createItem(item)) {
            case Success(itemId) => complete(StatusCodes.Created, s"Item created with ID: $itemId")
            case Failure(ex)     => complete(StatusCodes.InternalServerError, s"Failed to create Item: $ex")
          }
        }
      }
    }

  def createUserRoute: Route =
    path("user") {
      post {
        entity(as[User]) { user =>
          onComplete(userTableService.createUser(user)) {
            case Success(userId) => complete(StatusCodes.Created, s"User created with ID: $userId")
            case Failure(ex)     => complete(StatusCodes.InternalServerError, s"Failed to create User: $ex")
          }
        }
      }
    }

  def createAuctionRoute: Route = {
    path("auction" / "create") {
      post {
        entity(as[String]) { body =>
          Try(body.parseJson.convertTo[Auction]) match {
            case Success(auction) =>
              onComplete(bidTableService.createAuction(auction)) {
                case Success(response) =>
                  complete(StatusCodes.OK, response)
                case Failure(exception) =>
                  complete(StatusCodes.InternalServerError, s"Error: ${exception.getMessage}")
              }

            case Failure(exception) =>
              complete(StatusCodes.BadRequest, s"Invalid JSON format: ${exception.getMessage}")
          }
        }
      }
    }
  }


  def createBidRoute: Route =
    path("bid") {
      post {
        entity(as[Bid]) { bid =>
          println("bid: " + bid)
          bid match {
            case x: Bid =>
              println(x)
              onComplete(bidTableService.placeBid(x)) {
                case Success(bidId) =>
                  complete(StatusCodes.Created, s"$bidId")
                case Failure(ex) =>
                  complete(StatusCodes.InternalServerError, s"Failed to place Bid: $ex")
              }
            case _ => println("Doesn't match type")
              complete(StatusCodes.InternalServerError, "Failed to place Bid")
          }
        }
      }
    }


  def getAllItemsRoute: Route =
    path("items") {
      get {
        onComplete(itemTableService.getAllItems()) {
          case Success(items) => complete(StatusCodes.OK, items.toJson.prettyPrint)
          case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to fetch Items: $ex")
        }
      }
    }

  def getItemRoute: Route =
    path("item" / LongNumber) { itemId =>
      get {
        onComplete(itemTableService.getItemById(itemId)) {
          case Success(Some(item)) => complete(StatusCodes.OK, item.toJson.prettyPrint)
          case Success(None)       => complete(StatusCodes.NotFound, s"Item with ID $itemId not found")
          case Failure(ex)         => complete(StatusCodes.InternalServerError, s"Failed to fetch item: $ex")
        }
      }
    }
  def getUserRoute: Route =
    path("user" / LongNumber) { userId =>
      get {
        onComplete(userTableService.getUserById(userId)) {
          case Success(Some(user)) => complete(StatusCodes.OK, user.toJson.prettyPrint)
          case Success(None)       => complete(StatusCodes.NotFound, s"User with ID $userId not found")
          case Failure(ex)         => complete(StatusCodes.InternalServerError, s"Failed to fetch user details: $ex")
        }
      }
    }

  def getBidRoute: Route =
    path("bid" / LongNumber) { itemId =>
      get {
        onComplete(bidTableService.getBidsForItem(itemId)) {
          case Success(bids) if bids.nonEmpty=> complete(StatusCodes.OK, bids.toJson.prettyPrint)
          case Success(_)       => complete(StatusCodes.NotFound, s"Bid with item ID $itemId not found")
          case Failure(ex)         => complete(StatusCodes.InternalServerError, s"Failed to fetch bids: $ex")
        }
      }
    }
  def getAllUsersRoute: Route =
    path("users") {
      get {
        onComplete(userTableService.getAllUsers()) {
          case Success(users) => complete(StatusCodes.OK, users.toJson.prettyPrint)
          case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to fetch Users: $ex")
        }
      }
    }

  def getAllBidsRoute: Route =
    path("bids") {
      get {
        onComplete(bidTableService.getAllBids()) {
          case Success(bids) => complete(StatusCodes.OK, bids.toJson.prettyPrint)
          case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to fetch Bids: $ex")
        }
      }
    }

  def updateItemRoute: Route =
    path("item" / LongNumber) { itemId =>
      put {
        entity(as[Item]) { updatedItem =>
          onComplete(itemTableService.updateItem(itemId, updatedItem)) {
            case Success(_) => complete(StatusCodes.OK, "Item updated successfully.")
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to update Item: $ex")
          }
        }
      }
    }

  def updateUserRoute: Route =
    path("user" / LongNumber) { userId =>
      put {
        entity(as[User]) { updatedUser =>
          onComplete(userTableService.updateUser(userId, updatedUser)) {
            case Success(_) => complete(StatusCodes.OK, "User updated successfully.")
            case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to update User: $ex")
          }
        }
      }
    }


  def deleteItemRoute: Route =
    path("item" / LongNumber) { itemId =>
      delete {
        onComplete(itemTableService.deleteItem(itemId)) {
          case Success(_) => complete(StatusCodes.OK, "Item deleted successfully.")
          case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to delete Item: $ex")
        }
      }
    }

  def deleteUserRoute: Route =
    path("user" / LongNumber) { userId =>
      delete {
        onComplete(userTableService.deleteUser(userId)) {
          case Success(_) => complete(StatusCodes.OK, "User deleted successfully.")
          case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to delete User: $ex")
        }
      }
    }

  def deleteBidRoute: Route =
    path("bid" / LongNumber) { itemId =>
      delete {
        onComplete(bidTableService.deleteBidsForItem(itemId)) {
          case Success(_) => complete(StatusCodes.OK, "Bid deleted successfully.")
          case Failure(ex) => complete(StatusCodes.InternalServerError, s"Failed to delete Bid: $ex")
        }
      }
    }

  def auctionWebSocketRoute: Route = auctionWebSocketRoutes.route
  def route: Route = concat(
    createItemRoute, createUserRoute, createBidRoute,createAuctionRoute,
    getAllItemsRoute, getAllUsersRoute, getAllBidsRoute,getItemRoute,getBidRoute,getUserRoute,
    updateItemRoute, updateUserRoute,
    deleteItemRoute, deleteUserRoute, deleteBidRoute,
    auctionWebSocketRoute
  )
}