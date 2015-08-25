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

}
