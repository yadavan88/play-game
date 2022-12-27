package controllers
import models.User

import java.util.UUID
import scala.collection.mutable.Map

object UserSessionHandler {

  // hardcoded this just for testing, need to remove this later
  val session: Map[String, User] = Map(
    "uuid" -> User(1, "admin", "password123", true)
  ) // Map.empty

  def createSession(user: User): String = {
    val sessionId = UUID.randomUUID().toString
    session.put(sessionId, user)
    sessionId
  }

  def getUserFromSession(sessionKey: Option[String]): Option[User] = {
    sessionKey.flatMap(session.get)
  }

}
