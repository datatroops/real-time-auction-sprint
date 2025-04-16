package io.datatroops.services

import io.datatroops.entities.ItemTableDAO
import io.datatroops.models.Item
import scala.concurrent.Future

class ItemTableService(itemTableDAO: ItemTableDAO)() {
  def createItem(item: Item): Future[Long] = {
    itemTableDAO.createItem(item)
  }

  def getAllItems(): Future[Seq[Item]] = {
    itemTableDAO.getAllItems()
  }

  def getItemById(itemId: Long): Future[Option[Item]] = {
    itemTableDAO.getItemById(itemId)
  }

  def updateItem(itemId: Long, newItem: Item): Future[Int] = {
    itemTableDAO.updateItem(itemId, newItem)
  }

  def deleteItem(itemId: Long): Future[Int] = {
    itemTableDAO.deleteItem(itemId)
  }
}
