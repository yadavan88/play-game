package controllers
import dao.User

import java.util.UUID
import scala.collection.mutable.Map

object UserSession {

  val session: Map[String, User] = Map("uuid" -> User(1, "yadukrishnan", "password123", true)) //Map.empty

  def createSession(user: User): String = {
    val sessionId = UUID.randomUUID().toString
    session.put(sessionId, user)
    sessionId
  }

  def getUserFromSession(sessionKey: Option[String]): Option[User] = {
    sessionKey.flatMap(session.get)
  }

}
