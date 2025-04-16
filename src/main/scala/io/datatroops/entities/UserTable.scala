package io.datatroops.entities

import io.datatroops.models.User
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class UserTable(tag: Tag) extends Table[User](tag, Some("auctions"), "User") {
  def userId = column[Long]("user_id", O.PrimaryKey)
  def name = column[String]("name")
  def * = (userId, name).mapTo[User]
}

class UserTableDAO(db: Database)(implicit ec: ExecutionContext) {
  private val users = TableQuery[UserTable]

  def getSchema = users.schema

  def createUser(user: User): Future[Long] = {
    val insertQuery = (users returning users.map(_.userId)) += user
    db.run(insertQuery)
  }

  def getAllUsers(): Future[Seq[User]] = {
    db.run(users.result)
  }

  def getUserById(userId: Long): Future[Option[User]] = {
    db.run(users.filter(_.userId === userId).result.headOption)
  }

  def updateUser(userId: Long, newUser: User): Future[Int] = {
    val query = users.filter(_.userId === userId)
      .map(_.name)
      .update(newUser.name)
    db.run(query)
  }

  def deleteUser(userId: Long): Future[Int] = {
    db.run(users.filter(_.userId === userId).delete)
  }
}