package io.datatroops.models

import spray.json._

case class Item(itemId: Long, name: String, starting_price:BigDecimal,description: String)

object Item extends DefaultJsonProtocol {
  implicit val itemFormat: RootJsonFormat[Item] = jsonFormat4(Item.apply)
}