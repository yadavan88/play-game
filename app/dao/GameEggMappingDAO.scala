package dao

import models.GameEggMapping

import javax.inject.Inject
import slick.basic.DatabaseConfig
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider

import scala.concurrent.ExecutionContext
import slick.jdbc.JdbcProfile

import scala.concurrent.Future
import play.api.libs.json._

class GameEggMappingDAO @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val gameEggMappingTable = TableQuery[GameEggMappingTable]

  def getActiveGameIds(): Future[Seq[Int]] = {
    db.run(gameEggMappingTable.map(_.gameId).distinct.result)
  }

  def deleteGameProgress(gameId: Int): Future[Int] = {
    db.run(gameEggMappingTable.filter(_.gameId === gameId).delete)
  }

  def saveBatch(rows: Set[GameEggMapping]): Future[Option[Int]] = {
    db.run(gameEggMappingTable ++= rows)
  }

  def upvote(gameId: Int, pos: Int): Future[Boolean] = {
    val selectionQuery =
      gameEggMappingTable.filter(e =>
        e.gameId === gameId && e.eggPosition === pos
      )
    for {
      row <- db.run(selectionQuery.result.headOption)
      _ = require(
        row.isDefined,
        "Could not find the mapping row with the details provided"
      )
      _ <- db.run(selectionQuery.map(_.upvotes).update(row.get.upvotes + 1))
    } yield true
  }

  def getAllEggs(gameId: Int): Future[Seq[GameEggMapping]] = {
    db.run(gameEggMappingTable.filter(_.gameId === gameId).result)
  }

  def claim(gameId: Int, pos: Int, ownerId: Int): Future[Int] = {
    val selectQuery = gameEggMappingTable.filter(q =>
      q.gameId === gameId && q.eggPosition === pos
    )
    db.run(selectQuery.map(_.userId).update(Some(ownerId)))
  }

  def writeMessage(gameId: Int, pos: Int, newMsg: String) = {
    val selectionQuery =
      gameEggMappingTable.filter(e =>
        e.gameId === gameId && e.eggPosition === pos
      )
    for {
      row <- db.run(selectionQuery.result.headOption)
      _ = require(
        row.isDefined,
        "Could not find the mapping row with the details provided"
      )
      _ <- db.run(selectionQuery.map(_.message).update(Some(newMsg)))
    } yield true
  }

  def isGameInitialized(gameId: Int): Future[Boolean] = {
    db.run(gameEggMappingTable.filter(_.gameId === gameId).exists.result)
  }

  private class GameEggMappingTable(tag: Tag)
      extends Table[GameEggMapping](tag, "GameEggMapping") {
    def gameEggMappingId = column[Int](
      "gameEggMappingId",
      O.PrimaryKey,
      O.AutoInc,
      O.SqlType("SERIAL")
    )
    def gameId = column[Int]("gameId")
    def eggPosition = column[Int]("eggPosition")
    def userId = column[Option[Int]]("userId")
    def message = column[Option[String]]("message")
    def upvotes = column[Int]("upvotes")

    def * = (
      gameEggMappingId,
      gameId,
      eggPosition,
      userId,
      message,
      upvotes
    ) <> (GameEggMapping.tupled, GameEggMapping.unapply)
  }

  def scripts = gameEggMappingTable.schema.createIfNotExistsStatements
}
