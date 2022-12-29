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
          db.run(userTable += user).map(_ => true)
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

  def validateCredential(credential: Credentials): Future[Option[User]] = {
    val query = userTable.filter { u =>
      u.name === credential.username && u.pwd === credential.password
    }
    db.run(query.result.headOption)
  }

  private class UserTable(tag: Tag) extends Table[User](tag, "User") {
    def userId =
      column[Int]("userId", O.PrimaryKey, O.AutoInc, O.SqlType("SERIAL"))
    def name = column[String]("username")
    def pwd = column[String]("password")
    def active = column[Boolean]("active")
    def * = (userId, name, pwd, active) <> (User.tupled, User.unapply)
  }

  def scripts = userTable.schema.createIfNotExistsStatements
}
