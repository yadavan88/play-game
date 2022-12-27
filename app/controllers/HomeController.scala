package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import dao._
import dao.JsonImplicits._
import models._
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
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

  def gamePlayPage(gameId: Int) = Action.async { _ =>
    gameService.getGame(gameId).map {
      case Some(game) => Ok(views.html.gameplay(game))
      case None       => BadRequest(Html("<h3>Invalid Game Id passed!</h3>"))
    }
  }

  /** ------------------------- */

  def saveUser() = Action.async { implicit request: Request[AnyContent] =>
    val user: User = request.body.asJson.get.as[User]
    userDao.saveUser(user).map(res => Ok("Saved"))
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

  def initializeGame(gameId: Int) = Action.async {
    implicit request: Request[AnyContent] =>
      gameService
        .initializeGame(gameId)
        .map(_ => Ok("Initialized game: " + gameId))
  }

  def deleteGameProgress(gameId: Int) = Action.async {
    implicit request: Request[AnyContent] =>
      gameService
        .deleteGameProgress(gameId)
        .map(_ => Ok("Game progress cleared for game: " + gameId))
  }

  def generateScripts() = Action { implicit request: Request[AnyContent] =>
    generator.generateScripts
    Ok("Generated scripts")
  }

  private def authenticated(
      action: User => EssentialAction
  ): EssentialAction = {
    EssentialAction { request =>
      println("user check... ")
      val userOpt =
        UserSessionHandler.getUserFromSession(request.headers.get(SESSION_KEY))
      println(request.headers)
      userOpt match {
        case Some(user) => action(user)(request)
        case None       => Accumulator.done(Forbidden("Invalid apiKey"))
      }
    }
  }

  def withAuth[T](
      thunk: => Future[Result]
  )(implicit request: Request[AnyContent]) = {
    val user =
      UserSessionHandler.getUserFromSession(request.headers.get(SESSION_KEY))
    user
      .map(_ => thunk)
      .getOrElse(Future.successful(Forbidden("Invalid session key")))
  }

  def upvote(mappingId: Int) =
    authenticated { user =>
      Action.async { implicit request =>
        gameService.upvote(mappingId).map(_ => Ok("Upvoted successfully"))
      }
    }

  def reveal(gameId: Int, pos: Int) = {
    println("Inside the reveal method...... ")
    authenticated { user =>
      Action.async { implicit request =>
        gameService
          .reveal(gameId, pos, user.userId)
          .map(res => Ok(Json.toJson(res)))
      }
    }
  }

  final val SESSION_KEY = "sessionKey"

  def loginForm() = Action { implicit request =>
    Ok(views.html.loginform(LoginForm.form))
  }

  def validateLogin() = Action.async { implicit request =>
    println("before trying to login....")
    val cred = LoginForm.form.bindFromRequest().get
    // val cred = request.body.asJson.get.as[Credentials]
    userDao.validateCredential(cred).map { user =>
      if (user.isDefined) {
        val sessionKey = UserSessionHandler.createSession(user.get)
        Ok(views.html.afterlogin(user.get))
          .withHeaders(SESSION_KEY -> sessionKey)
      } else {
        Unauthorized("Invalid credentials!")
      }
    }
  }

  def findEgg() = {
    // Security.WithAuthentication{ req =>
    //   req.headers.get("apiKey").map{ key => }
    // }
  }
}
//https://pedrorijo.com/blog/scala-play-auth/

object LoginForm {
  val form = Form(
    mapping(
      "username" -> text,
      "password" -> text
    )(Credentials.apply)(Credentials.unapply)
  )
}
