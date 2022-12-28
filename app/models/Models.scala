package models

case class Credentials(username: String, password: String)
case class Game(gameId: Int, name: String)
case class GameInfo(game: Game, initialized: Boolean)
case class User(
                 userId: Int,
                 username: String,
                 password: String,
                 active: Boolean = true
               )

case class GameEggMapping(
                           gameEggMappingId: Int,
                           gameId: Int,
                           eggPosition: Int,
                           userId: Option[Int],
                           message: Option[String],
                           upvotes: Int
                         )

final case class RevealResponse(
                                 alreadyClaimed: Boolean,
                                 message: Option[String],
                                 user: Option[User],
                                 upvotes: Int
                               )


object Exceptions {
  final case class DuplicateUsernameException(message: String) extends Exception
}