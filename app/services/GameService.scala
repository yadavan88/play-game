package services

import javax.inject.Inject
import dao.{GameDAO, GameEggMappingDAO, UserDAO}
import models._
import models.{Game, GameEggMapping, User}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util.Random

class GameService @Inject() (
    val gameDao: GameDAO,
    gameEggMappingDAO: GameEggMappingDAO,
    userDAO: UserDAO
)(implicit
    executionContext: ExecutionContext
) {

  private final val MAX_NO_OF_EGGS = 6

  def getAllGames(): Future[Seq[Game]] = gameDao.getAllGames()

  def createGame(game: Game): Future[String] = gameDao.createGame(game)

  // This can be done using join query, but using normal scala mapping here for this example
  def getActiveGames(): Future[Seq[Game]] = for {
    gameIds <- gameEggMappingDAO.getActiveGameIds()
    allGames <- getAllGames()
  } yield allGames.filter(gameIds.contains)

  def getInactiveGames(): Future[Seq[Game]] = for {
    gameIds <- gameEggMappingDAO.getActiveGameIds()
    allGames <- getAllGames()
  } yield allGames.filterNot(gameIds.contains)

  def getGame(gameId: Int): Future[Option[Game]] = {
    getAllGames().map(_.find(_.gameId == gameId))
  }

  def initializeGame(gameId: Int): Future[Boolean] = {
    for {
      activeGames <- gameEggMappingDAO.getActiveGameIds()
      _ = if (activeGames.contains(gameId)) {
        throw new IllegalStateException(
          "This game is already initialized, can't initialize again! Delete the game progress and then try again"
        )
      }
      // initialise game by generating random easter eggs (positions : 1 - 100)
      eggPositions = generateEggPositions
      rows = eggPositions.map(pos =>
        GameEggMapping(0, gameId, pos, None, None, 0)
      )
      _ <- gameEggMappingDAO.saveBatch(rows)
      _ = println(s"Generated and hid ${rows.size} eggs for the game: $gameId")
    } yield true
  }

  private def generateEggPositions: Set[Int] = {
    // Adding +5 to reduce the chance of duplicate numbers causing the egg count to fall low
    val random = Set.fill(MAX_NO_OF_EGGS + 5)(Random.nextInt(25))
    random.take(MAX_NO_OF_EGGS)
  }

  def deleteGameProgress(gameId: Int): Future[Int] = {
    gameEggMappingDAO.deleteGameProgress(gameId)
  }

  def reveal(gameId: Int, pos: Int, userId: Int): Future[RevealResponse] = {
    for {
      eggs <- gameEggMappingDAO.getAllEggs(gameId)
      position = eggs.find(_.eggPosition == pos)
      user <- userDAO.getUser(userId)
      response <-
        if (position.isEmpty) {
          Future.failed(new Exception("Invalid Position, bad luck"))
        } else if (position.flatMap(_.userId).isDefined) {
          // Already owned by a user
          Future.successful(
            RevealResponse(
              true,
              position.flatMap(_.message),
              user,
              position.get.upvotes
            )
          )
        } else {
          gameEggMappingDAO.claim(gameId, pos, userId).map { _ =>
            RevealResponse(false, None, user, 0)
          }
        }
    } yield response
  }

  def upvote(mappingId: Int): Future[Boolean] = {
    gameEggMappingDAO.upvote(mappingId)
  }

}
