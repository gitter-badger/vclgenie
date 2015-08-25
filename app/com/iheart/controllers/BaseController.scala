package com.iheart.controllers

import com.iheart.json.Formats._
import com.iheart.models.Rule
import play.Logger
import play.api.libs.json.{JsSuccess, Json, JsError}
import play.api.mvc.Results._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


trait BaseController {

  implicit class errorJClass(jserror: JsError) {

    def errorJF  = Future {
      val msgs = jserror.errors.flatMap { e =>
        e._2.map { v  =>
          v.message
        }
      }
      BadRequest(Json.toJson(RuleError(msgs)))
    }
  }

  implicit class errorRuleClass(ruleError: RuleError) {

    def errorJF = Future {
      BadRequest(Json.toJson(ruleError))
    }
  }

  implicit class successOkClass(str: String) {
    def successF = Future {
      Logger.info("Sending OK")
      Ok(str)
    }
  }

  implicit class validRulesClass(seq: JsSuccess[Seq[Either[RuleError,Rule]]]) {
    def isValid: Boolean =
      seq.getOrElse(Seq()).count(s => s.isLeft) == 0

    def errorJF = Future {
      BadRequest(Json.toJson(seq.getOrElse(Seq()).filter(s => s.isLeft).map(r => r.left.get)))
    }
  }

  implicit class validVclRequestClass(req: JsSuccess[VclRequest]) {
    def isValid: Boolean =
      req.get.rules.count(s => s.isLeft) == 0 && req.get.hostnames.count(h => h.isLeft) == 0

    def toRules = req.get.rules.filter(res => res.isRight).map(res => res.right.get)

    def toHostnames = req.get.hostnames.filter(res => res.isRight).map(res => res.right.get)

    def errorToString[T <: BaseError](c: Seq[T]): Seq[String] = c.flatMap(e => e.errors)

    def errorJF = Future {
      val ruleErrors = req.get.rules.filter(s => s.isLeft).map(r => r.left.get)
      val hostnameErrors = req.get.hostnames.filter(h => h.isLeft).map(r => r.left.get)
      val errors: Seq[String] = errorToString(ruleErrors) ++ errorToString(hostnameErrors)
      BadRequest(Json.obj("errors" -> errors ))
    }
  }

}
