package controllers

import models.{Credentials, User}
import play.api.data.Form
import play.api.data.Forms._

object LoginForm {
  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Credentials.apply)(Credentials.unapply).verifying(
      "Password too short",
      user => user.password.length > 1
    )
  )
}
object UserForm {
  // we are using this form only to create the UI. We are not submitting the form directly.
  // Instead, we are manipulating the results and generate JSON to send POST request
  // Hence, adding form validation will not work, except for showing the details in the page
  val form = Form(
    mapping(
      "userId" -> number,
      "username" -> nonEmptyText(minLength = 5),
      "password" -> nonEmptyText(minLength = 5),
      "salt" -> ignored(Option.empty[String]),
      "active" -> boolean
    )(User.apply)(User.unapply)
  )
}
