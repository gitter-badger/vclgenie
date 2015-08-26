package com.iheart.controllers

import com.iheart.vcl.VclGenerator
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
  /// val req = request.body.validate[Seq[Either[RuleError,Rule]]]
    val req = request.body.validate[Either[RequestError,VclRequest]]

    req match {
      case success: JsSuccess[Either[RequestError,VclRequest]] => success.isValid match {
        case true =>
          val orules: Seq[Rule] = success.toOrderedRules
          val grules: Seq[Rule] = success.toGlobalRules
          val hostnames: Seq[Hostname] = success.toHostnames
          val v = new VclGenerator
          v.generateRuleset(hostnames,orules, grules).successF
        case false =>  success.errorJF
      }
      case e: JsError =>  e.errorJF
    }

  }


}
