package dao

import play.api.libs.json.Json
import models._

object JsonImplicits {
  implicit val userFormat = Json.format[User]
  implicit val gameFormat = Json.format[Game]
  implicit val gameInfoFormat = Json.format[GameInfo]
  implicit val mappingFormat = Json.format[GameEggMapping]
  implicit val credentialFormat = Json.format[Credentials]
  implicit val revealResponseFormat = Json.format[RevealResponse]
  implicit val secretMessageFormat = Json.format[SecretMessage]
}
