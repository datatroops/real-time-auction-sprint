package entities

import io.datatroops.entities.UserTableDAO
import io.datatroops.models.User
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

class UserTableDAOTest extends AsyncFlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  val db = Database.forConfig("testdb")
  val userTableDAO = new UserTableDAO(db)
  val userId = (scala.util.Random.nextInt(1000)+1).toLong

  override def beforeAll(): Unit = {
    val setup = DBIO.seq(
      userTableDAO.getSchema.createIfNotExists
    )
    Await.result(db.run(setup), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.shutdown, Duration.Inf)
  }

  "UserTableDAO" should "create a user and return the user ID" in {

    val user = User(userId, "Lakshay-test")
    userTableDAO.createUser(user).map { userId =>
      userId should be > 0L
    }
  }

  it should "retrieve all users" in {
    userTableDAO.getAllUsers().map { users =>
      users should not be empty
    }
  }

  it should "retrieve a user by ID" in {
    userTableDAO.getUserById(userId).map { userOpt =>
      userOpt shouldBe defined
      userOpt.get.userId shouldBe userId
      userOpt.get.name shouldBe "Lakshay-test"
    }
  }

  it should "update a user's name" in {
    val updatedUser = User(userId, "Jane Doe")
    userTableDAO.updateUser(userId, updatedUser).flatMap { updateCount =>
      updateCount shouldBe 1
      userTableDAO.getUserById(userId).map { userOpt =>
        userOpt shouldBe defined
        userOpt.get.name shouldBe "Jane Doe"
      }
    }
  }

  it should "delete a user" in {
    userTableDAO.deleteUser(userId).flatMap { deleteCount =>
      deleteCount shouldBe 1
      userTableDAO.getUserById(userId).map { userOpt =>
        userOpt shouldBe empty
      }
    }
  }
}
