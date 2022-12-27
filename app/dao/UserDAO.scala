package dao

import models.{Credentials, User}

import javax.inject.Inject
import slick.basic.DatabaseConfig
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.ExecutionContext
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import play.api.libs.json._

class UserDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._
  private val userTable = TableQuery[UserTable]

  def saveUser(user: User): Future[Boolean] =
    db.run(userTable += user).map(_ => true)

  def getUsers(all: Boolean): Future[Seq[User]] = {
    val query =
      if (all) userTable.result else userTable.filter(_.active === true).result
    db.run(query)
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
