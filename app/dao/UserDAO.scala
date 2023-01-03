package dao

import models.Exceptions.DuplicateUsernameException
import models.{Credentials, User}
import play.api.cache.AsyncCacheApi

import javax.inject.Inject
import slick.basic.DatabaseConfig
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.ExecutionContext
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import play.api.libs.json._

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UserDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    cache: AsyncCacheApi
)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  private val userTable = TableQuery[UserTable]

  def saveUser(user: User): Future[Boolean] = {
    for {
      users <- getUsers(true)
      res <-
        if (users.exists(_.username == user.username)) {
          Future.failed(
            DuplicateUsernameException(
              s"Username `${user.username}` is already taken!"
            )
          )
        } else {
          val salt = generateSalt()
          println("salt = " + salt)
          val hashedPwd = hashPassword(user.password, salt)
          val updatedUser =
            user.copy(password = hashedPwd, salt = Some(salt))
          db.run(userTable += updatedUser).map(_ => true)
        }
    } yield res

  }

  private def getUsersInternal: Future[Seq[User]] = {
    db.run(userTable.result).map { users: Seq[User] =>
      // Setting the rows to cache when accessed for the first time
      cache.set("users", users)
      users
    }
  }
  def getUsers(all: Boolean): Future[Seq[User]] = {
    cache.getOrElseUpdate("users") {
      getUsersInternal.map { users =>
        if (all) users else users.filter(_.active == true)
      }
    }

  }

  def getUser(userId: Int): Future[Option[User]] = {
    getUsers(true).map(_.find(_.userId == userId))
  }

  // Not considering any hashing of password for easy impl. Not to be done this way in any prod applications
  def validateCredential(credential: Credentials): Future[User] = {
    val query = userTable.filter { u =>
      u.name === credential.username
    }
    for {
      user <- db.run(query.result.headOption)
      _ = if (user.isEmpty) {
        throw new Exception("Invalid username")
      }
      hashedPwd = hashPassword(
        credential.password,
        user.flatMap(_.salt).get
      )
      _ = if (hashedPwd != user.get.password) {
        throw new Exception("Invalid Credentials")
      }
    } yield user.get

  }

  private def generateSalt(): String = {
    val random = new SecureRandom()
    val saltBytes = new Array[Byte](16)
    random.nextBytes(saltBytes)
    Base64.getEncoder.encodeToString(saltBytes)
  }

  def hashPassword(password: String, salt: String): String = {
    val saltBytes = Base64.getDecoder.decode(salt)
    val spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 128)
    val secretFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    val hashBytes = secretFactory.generateSecret(spec).getEncoded()
    Base64.getEncoder.encodeToString(hashBytes)
  }

  private class UserTable(tag: Tag) extends Table[User](tag, "User") {
    def userId =
      column[Int]("userId", O.PrimaryKey, O.AutoInc, O.SqlType("SERIAL"))
    def name = column[String]("username")
    def pwd = column[String]("password")
    def salt = column[Option[String]]("salt")
    def active = column[Boolean]("active")
    def * = (userId, name, pwd, salt, active) <> (User.tupled, User.unapply)
  }

  def scripts = userTable.schema.createIfNotExistsStatements
}
