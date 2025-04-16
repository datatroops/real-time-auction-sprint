package io.datatroops.models

import spray.json._

case class User (userId: Long, name: String)

object User extends DefaultJsonProtocol {
  implicit val userFormat: RootJsonFormat[User] = jsonFormat2(User.apply)
}
