package io.datatroops.services

import io.datatroops.entities.UserTableDAO
import io.datatroops.models.User

import scala.concurrent.Future

class UserTableService(userTableDAO: UserTableDAO)() {

  def createUser(user: User): Future[Long] = {
    userTableDAO.createUser(user)
  }

  def getAllUsers(): Future[Seq[User]] = {
    userTableDAO.getAllUsers()
  }

  def getUserById(userId: Long): Future[Option[User]] = {
    userTableDAO.getUserById(userId)
  }

  def updateUser(userId: Long, newUser: User): Future[Int] = {
    userTableDAO.updateUser(userId, newUser)
  }

  def deleteUser(userId: Long): Future[Int] = {
    userTableDAO.deleteUser(userId)
  }
}
