package com.iheart.controllers

import play.api._
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.libs.Json._
import com.iheart.models._
import com.iheart.json.Formats._

import scala.concurrent.Future

class VclController extends Controller with BaseController {

  def index = Action.async {
    Future {Ok(views.html.index())}
  }

  def generate = Action.async(parse.json) { request =>
   val req = request.body.validate[Seq[Either[RuleError,Rule]]]

    req match {
      case s: JsSuccess[Seq[Either[RuleError,Rule]]] => s.isValid match {
        case true =>  "worked".successF
        case false =>  s.errorJF
      }
      case e: JsError =>  Logger.info("ERROR:" + e.errorJF.toString); e.errorJF
    }

  }


}
