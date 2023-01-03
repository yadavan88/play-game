package dao

import play.api.libs.json.{Json, Writes}
import models._

object JsonImplicits {
  implicit val userReadFormat = Json.reads[User]
  implicit val userWriteFormat: Writes[User] = Writes { user =>
    Json.obj(
      "userId" -> user.userId,
      "username" -> user.username,
      "password" -> "*****",
      "salt" -> "******",
      "active" -> user.active
    )
  }
  implicit val gameFormat = Json.format[Game]
  implicit val gameInfoFormat = Json.format[GameInfo]
  implicit val mappingFormat = Json.format[GameEggMapping]
  implicit val credentialFormat = Json.format[Credentials]
  implicit val revealResponseFormat = Json.format[RevealResponse]
  implicit val secretMessageFormat = Json.format[SecretMessage]
}
