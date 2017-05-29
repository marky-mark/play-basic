package controllers

import play.api.mvc._

import scala.concurrent.Future

class HealthCheck extends Controller {
  def get() = Action.async { implicit request =>
    Future.successful(Results.Ok)
  }
}

