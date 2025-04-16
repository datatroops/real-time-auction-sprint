package services

import io.datatroops.entities.ItemTableDAO
import io.datatroops.models.Item
import io.datatroops.services.ItemTableService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class ItemTableServiceTest
  extends AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global

  "ItemTableService" should {

    "create an item successfully" in {
      val mockItemTableDAO = mock[ItemTableDAO]
      val itemService = new ItemTableService(mockItemTableDAO)
      val item = Item(1L, "Laptop", 22000.50,"A high-end gaming laptop")
      when(mockItemTableDAO.createItem(any[Item])).thenReturn(Future.successful(1L))
      val result = itemService.createItem(item).futureValue
      result shouldBe 1L
      verify(mockItemTableDAO).createItem(item)
    }

    "retrieve all items" in {
      val mockItemTableDAO = mock[ItemTableDAO]
      val itemService = new ItemTableService(mockItemTableDAO)
      val items = Seq(
        Item(1L, "Phone", 70000.00,"A smartphone with 128GB storage"),
        Item(2L, "Headphones", 3000.00,"Wireless noise-cancelling headphones")
      )
      when(mockItemTableDAO.getAllItems()).thenReturn(Future.successful(items))
      val result = itemService.getAllItems().futureValue
      result should have size 2
      result should contain theSameElementsAs items
      verify(mockItemTableDAO).getAllItems()
    }

    "retrieve an item by ID" in {
      val mockItemTableDAO = mock[ItemTableDAO]
      val itemService = new ItemTableService(mockItemTableDAO)
      val item = Item(1L, "Tablet", 12000.00,"A powerful tablet for work and entertainment")
      when(mockItemTableDAO.getItemById(1L)).thenReturn(Future.successful(Some(item)))
      val result = itemService.getItemById(1L).futureValue
      result shouldBe Some(item)
      verify(mockItemTableDAO).getItemById(1L)
    }

    "return None when item ID does not exist" in {
      val mockItemTableDAO = mock[ItemTableDAO]
      val itemService = new ItemTableService(mockItemTableDAO)
      when(mockItemTableDAO.getItemById(99L)).thenReturn(Future.successful(None))
      val result = itemService.getItemById(99L).futureValue
      result shouldBe None
      verify(mockItemTableDAO).getItemById(99L)
    }

    "update an item successfully" in {
      val mockItemTableDAO = mock[ItemTableDAO]
      val itemService = new ItemTableService(mockItemTableDAO)
      val updatedItem = Item(1L, "Updated Laptop", 23000.00,"An upgraded gaming laptop")
      when(mockItemTableDAO.updateItem(any[Long], any[Item])).thenReturn(Future.successful(1))
      val result = itemService.updateItem(1L, updatedItem).futureValue
      result shouldBe 1
      verify(mockItemTableDAO).updateItem(1L, updatedItem)
    }

    "delete an item successfully" in {
      val mockItemTableDAO = mock[ItemTableDAO]
      val itemService = new ItemTableService(mockItemTableDAO)
      when(mockItemTableDAO.deleteItem(1L)).thenReturn(Future.successful(1))
      val result = itemService.deleteItem(1L).futureValue
      result shouldBe 1
      verify(mockItemTableDAO).deleteItem(1L)
    }
  }
}
