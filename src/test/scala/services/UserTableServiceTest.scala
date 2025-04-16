package services

import io.datatroops.entities.UserTableDAO
import io.datatroops.models.User
import io.datatroops.services.UserTableService
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.{ExecutionContext, Future}

class UserTableServiceTest
  extends AnyWordSpecLike
    with Matchers
    with MockitoSugar
    with BeforeAndAfterAll
    with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global

  "UserTableService" should {

    "create a user successfully" in {
      val mockUserTableDAO = mock[UserTableDAO]
      val userService = new UserTableService(mockUserTableDAO)
      val user = User(1L, "John Doe")
      when(mockUserTableDAO.createUser(any[User])).thenReturn(Future.successful(1L))
      val result = userService.createUser(user).futureValue
      result shouldBe 1L
      verify(mockUserTableDAO).createUser(user)
    }

    "retrieve all users" in {
      val mockUserTableDAO = mock[UserTableDAO]
      val userService = new UserTableService(mockUserTableDAO)
      val users = Seq(
        User(1L, "qwerty123"),
        User(2L, "qwerty")
      )
      when(mockUserTableDAO.getAllUsers()).thenReturn(Future.successful(users))
      val result = userService.getAllUsers().futureValue

      result should have size 2
      result should contain theSameElementsAs users
      verify(mockUserTableDAO).getAllUsers()
    }

    "retrieve a user by ID" in {
      val mockUserTableDAO = mock[UserTableDAO]
      val userService = new UserTableService(mockUserTableDAO)
      val user = User(1L, "abcdef")
      when(mockUserTableDAO.getUserById(1L)).thenReturn(Future.successful(Some(user)))
      val result = userService.getUserById(1L).futureValue
      result shouldBe Some(user)
      verify(mockUserTableDAO).getUserById(1L)
    }

    "return None when user ID does not exist" in {
      val mockUserTableDAO = mock[UserTableDAO]
      val userService = new UserTableService(mockUserTableDAO)
      when(mockUserTableDAO.getUserById(99L)).thenReturn(Future.successful(None))
      val result = userService.getUserById(99L).futureValue
      result shouldBe None
      verify(mockUserTableDAO).getUserById(99L)
    }

    "update a user successfully" in {
      val mockUserTableDAO = mock[UserTableDAO]
      val userService = new UserTableService(mockUserTableDAO)
      val updatedUser = User(1L, "Updated Name")
      when(mockUserTableDAO.updateUser(any[Long], any[User])).thenReturn(Future.successful(1))
      val result = userService.updateUser(1L, updatedUser).futureValue
      result shouldBe 1
      verify(mockUserTableDAO).updateUser(1L, updatedUser)
    }

    "delete a user successfully" in {
      val mockUserTableDAO = mock[UserTableDAO]
      val userService = new UserTableService(mockUserTableDAO)
      when(mockUserTableDAO.deleteUser(1L)).thenReturn(Future.successful(1))
      val result = userService.deleteUser(1L).futureValue
      result shouldBe 1
      verify(mockUserTableDAO).deleteUser(1L)
    }
  }
}
