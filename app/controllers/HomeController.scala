package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import dao._
import dao.JsonImplicits._
import models.Exceptions.{DuplicateUsernameException, NoEggException}
import models._
import play.api.data.Form
import play.api.data.Forms.{boolean, mapping, nonEmptyText, number, text}
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.twirl.api.Html
import services._

import scala.concurrent.Future

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (
    val controllerComponents: ControllerComponents,
    val userDao: UserDAO,
    generator: ScriptGenerator,
    gameService: GameService
) extends BaseController
    with play.api.i18n.I18nSupport {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method will be
    * called when the application receives a `GET` request with a path of `/`.
    */
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  def index() = Action.async { implicit request: Request[AnyContent] =>
    userDao.getUsers(true).map(users => Ok(views.html.index()))
  }

  def gamesListing() = Action.async { _ =>
    gameService
      .getAllGames()
      .map(games => Ok(views.html.gameslist(games.toList)))
  }

  def gamePlayPage(gameId: Int) = Action.async { implicit req =>
    gameService.getGame(gameId).map { game =>
      Ok(views.html.gameplay(game))
    }
  }

  def saveUser() = Action.async { implicit request: Request[AnyContent] =>
    val user: User = request.body.asJson.get.as[User]
    userDao
      .saveUser(user)
      .map(res => Redirect(routes.HomeController.loginForm()))
      .recover {
        case dup: DuplicateUsernameException =>
          Conflict(dup.message)
        case ex =>
          ex.printStackTrace()
          InternalServerError("Something went wrong, " + ex.getMessage)
      }
  }

  def users(all: Boolean) = Action.async {
    implicit request: Request[AnyContent] =>
      userDao.getUsers(all).map(res => Ok(Json.toJson(res)))
  }

  def getGames() = Action.async { _ =>
    gameService.getAllGames().map(res => Ok(Json.toJson(res)))
  }

  def saveGame() = Action.async { implicit request: Request[AnyContent] =>
    val game: Game = request.body.asJson.get.as[Game]
    gameService.createGame(game).map(res => Ok("New Game Created"))
  }

  def initializeGame(gameId: Int) = authenticated { user =>
    Action.async { implicit request: Request[AnyContent] =>
      gameService
        .initializeGame(gameId)
        .map(_ => Ok("Initialized game: " + gameId))
    }
  }

  def deleteGameProgress(gameId: Int) = authenticated { user =>
    Action.async { implicit request: Request[AnyContent] =>
      gameService
        .deleteGameProgress(gameId)
        .map(_ => Ok("Game progress cleared for game: " + gameId))
    }
  }

  def generateScripts() = Action { implicit request: Request[AnyContent] =>
    generator.generateScripts
    Ok("Generated scripts")
  }

  private def authenticated(
      action: User => EssentialAction
  ): EssentialAction = {
    EssentialAction { request =>
      val key = request.cookies.get(SESSION_KEY).map(_.value)
      val userOpt =
        UserSessionHandler.getUserFromSession(key)
      userOpt match {
        case Some(user) => action(user)(request)
        case None =>
          Accumulator.done(
            Forbidden("Invalid apiKey, please re-login and try")
          )
      }
    }
  }

  def upvote(gameId: Int, pos: Int) =
    authenticated { user =>
      Action.async { implicit request =>
        gameService.upvote(gameId, pos).map(_ => Ok("Upvoted successfully"))
      }
    }

  def writeMessage(gameId: Int, pos: Int) =
    authenticated { user =>
      Action.async { implicit request =>
        val msg = request.body.asJson.get.as[SecretMessage]
        gameService
          .writeMessage(gameId, pos, msg.msg)
          .map(_ => Ok("Upvoted successfully"))
      }
    }

  def reveal(gameId: Int, pos: Int) = {
    authenticated { user =>
      Action.async { implicit request =>
        gameService
          .reveal(gameId, pos, user.userId)
          .map(res => Ok(Json.toJson(res)))
          .recover {
            case noEgg: NoEggException => NotFound(noEgg.msg)
            case ex: Exception         => InternalServerError(ex.getMessage)
          }
      }
    }
  }

  final val SESSION_KEY = "sessionKey"

  def loginForm() = Action { implicit request =>
    Ok(views.html.loginform(LoginForm.form))
  }

  def createUserForm() = Action { implicit request =>
    Ok(views.html.createuser(UserForm.form))
  }

  def validateLogin() = Action.async { implicit request =>
    LoginForm.form
      .bindFromRequest()
      .fold(
        er => {
          Future.successful(Ok(views.html.loginform(er)))
        },
        u => {
          userDao
            .validateCredential(u)
            .map { user =>
              val sessionKey = UserSessionHandler.createSession(user)
              Ok(views.html.index())
                // .withHeaders(SESSION_KEY -> sessionKey)
                .withCookies(Cookie(SESSION_KEY, sessionKey))
            }
            .recover { case ex =>
              Redirect("/login")
                .flashing("err" -> "Invalid Credentials")
            }
        }
      )
  }

  def findEgg() = {
    // Security.WithAuthentication{ req =>
    //   req.headers.get("apiKey").map{ key => }
    // }
  }
}
//https://pedrorijo.com/blog/scala-play-auth/
