package com.iheart.json

import com.iheart.models._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.libs.Json._
import play.api.libs.functional.syntax._


object Formats {

  implicit val ruleConditionFormat: Reads[Either[RuleError,RuleCondition]] = (
    ( JsPath \ "condition").read[String] and
    (JsPath \ "matcher").read[String] and
      (JsPath \ "value").read[String] and
      (JsPath \ "name").readNullable[String]
    )(RuleCondition.build _)

  implicit val ruleActionFormat: Reads[Either[RuleError,RuleAction]] = (
    ( JsPath \ "action").read[String] and
      (JsPath \ "name").readNullable[String] and
      (JsPath \ "value").readNullable[String]
    )(RuleAction.build _)

  implicit val ruleFormat = (
    (JsPath \ "conditions").read[Seq[Either[RuleError,RuleCondition]]] and
      (JsPath \ "actions").read[Seq[Either[RuleError,RuleAction]]] and
      (JsPath \ "match_type").read[String]
    )(Rule.build _ )

  class ReadsWithRequiredArgs[A](delegate: Reads[A]) extends Reads[A] {
    def reads(json: JsValue) = {
      try delegate.reads(json) catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }
  }

  case class RuleError(errors: Seq[String])

  implicit val errorWrites: Writes[RuleError] = Json.writes[RuleError]
}

