package dao

import javax.inject.Inject
import slick.basic.DatabaseConfig
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import scala.concurrent.ExecutionContext
import slick.jdbc.JdbcProfile
import scala.concurrent.Future
import play.api.libs.json._

case class Game(gameId: Int, name: String)

class GameDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._
  private val gameTable = TableQuery[GameTable]

  def getAllGames(): Future[Seq[Game]] = {
    db.run(gameTable.result)
  }

  def createGame(game: Game): Future[String] = {
    db.run(gameTable += game).map(_ => "Game created")
  }

  private class GameTable(tag: Tag) extends Table[Game](tag, "Game") {
    def gameId = column[Int]("gameId", O.PrimaryKey, O.AutoInc, O.SqlType("SERIAL"))
    def name = column[String]("name")
    def * = (gameId, name) <> (Game.tupled, Game.unapply)
  }

  def scripts = gameTable.schema.createIfNotExistsStatements
}
