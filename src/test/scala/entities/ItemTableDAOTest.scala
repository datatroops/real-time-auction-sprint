package entities

import io.datatroops.entities.ItemTableDAO
import io.datatroops.models.Item
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class ItemTableDAOTest extends AsyncFlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  val db = Database.forConfig("testdb")
  val itemTableDAO = new ItemTableDAO(db)

  override def beforeAll(): Unit = {
    val setup = DBIO.seq(
      itemTableDAO.getSchema.createIfNotExists
    )
    Await.result(db.run(setup.transactionally), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.shutdown, Duration.Inf)
  }

  "ItemTableDAO" should "create an item and return the item ID" in {
    val item = Item(1L, "Antique Vase", BigDecimal(100.00), "A rare vintage vase")
    itemTableDAO.createItem(item).map { itemId =>
      itemId should be > 0L
    }
  }

  it should "retrieve all items" in {
    itemTableDAO.getAllItems().map { items =>
      items should not be empty
    }
  }

  it should "retrieve an item by ID" in {
    val itemId = 1L
    itemTableDAO.getItemById(itemId).map { itemOpt =>
      itemOpt shouldBe defined
      itemOpt.get.itemId shouldBe itemId
      itemOpt.get.name shouldBe "Antique Vase"
    }
  }



  it should "delete an item" in {
    val itemId = 1L
    itemTableDAO.deleteItem(itemId).flatMap { deleteCount =>
      deleteCount shouldBe 1
      itemTableDAO.getItemById(itemId).map { itemOpt =>
        itemOpt shouldBe empty
      }
    }
  }

  it should "update an item's details" in {
    val itemId = (scala.util.Random.nextInt(1000)+1).toLong
    val oldItem = Item(itemId, "Mobile Phone",400.00,"Nice")
    itemTableDAO.createItem(oldItem)
    val updatedItem = Item(itemId, "Laptop", 150.00, "Good")
    itemTableDAO.updateItem(itemId, updatedItem).flatMap { updateCount =>
      updateCount shouldBe 1
      itemTableDAO.getItemById(itemId).map { itemOpt =>
        itemOpt shouldBe defined
        itemOpt.get.name shouldBe "Laptop"
        itemOpt.get.description shouldBe "Good"
        itemOpt.get.starting_price shouldBe BigDecimal(150.00)
      }
    }
  }
}
