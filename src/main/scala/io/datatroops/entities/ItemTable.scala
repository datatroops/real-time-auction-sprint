package io.datatroops.entities

import io.datatroops.models.Item
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

class ItemTable(tag: Tag) extends Table[Item](tag, Some("auctions"), "Item") {
  def itemId = column[Long]("item_id", O.PrimaryKey)
  def name = column[String]("name")
  def starting_price = column[BigDecimal]("starting_price")
  def description = column[String]("description")
  def * = (itemId, name, starting_price,description).mapTo[Item]
}
class ItemTableDAO(db: Database)() {
  private val items = TableQuery[ItemTable]

  def getSchema = items.schema
  def createItem(item: Item): Future[Long] = {
    val insertQuery = (items returning items.map(_.itemId)) += item
    db.run(insertQuery)
  }

  def getAllItems(): Future[Seq[Item]] = {
    db.run(items.result)
  }

  def getItemById(itemId: Long): Future[Option[Item]] = {
    db.run(items.filter(_.itemId === itemId).result.headOption)
  }

  def updateItem(itemId: Long, newItem: Item): Future[Int] = {
    val query = items.filter(_.itemId === itemId)
      .map(item => (item.name, item.description, item.starting_price))
      .update(newItem.name, newItem.description, newItem.starting_price)
    db.run(query)
  }

  def deleteItem(itemId: Long): Future[Int] = {
    db.run(items.filter(_.itemId === itemId).delete)
  }
}
