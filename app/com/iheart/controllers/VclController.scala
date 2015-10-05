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
    val req = request.body.validate[Either[RequestError,VclRequest]]

    req match {
        // The JSON is valid
      case success: JsSuccess[Either[RequestError,VclRequest]] => success.isValid match {
         //valid json and no validation errors
        case true =>
          val orules: Seq[Rule] = success.toOrderedRules
          val grules: Seq[Rule] = success.toGlobalRules
          val hostnames: Seq[Hostname] = success.toHostnames
          val backends: Seq[Backend] = success.toBackends
          val v = new VclGenerator
          v.generateRuleset(hostnames,orules, grules,backends).successF
         //valid json but validation errors.  We need to lift all the BaseErrors
        case false =>  success.errorJF
      }
      //invalid JSON
      case e: JsError =>  e.errorJF
    }

  }


  def config = Action.async { implicit request =>
    val nvConditions = VclConfigCondition.nameValueConditions
    val svConditions = VclConfigCondition.singleValConditions
    val actions = VclConfigAction.actionMap

    Future { Ok(Json.toJson(ConfigResponse(nvConditions,svConditions,actions))) }
  }


}
