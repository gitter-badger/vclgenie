package com.iheart.json

import com.iheart.models._
import play.Logger
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
      (JsPath \ "value").readNullable[String] and
      (JsPath \ "units").readNullable[String]
    )(RuleAction.build _)

  implicit val ruleFormat = (
    (JsPath \ "conditions").read[Seq[Either[RuleError,RuleCondition]]] and
      (JsPath \ "actions").read[Seq[Either[RuleError,RuleAction]]] and
      (JsPath \ "match_type").read[String] and
      (JsPath \ "index").readNullable[Int]
    )(Rule.build _ )


  implicit val hostnameFormat: Reads[Either[HostnameError,Hostname]] =
    ( JsPath \ "hostname").read[String].map(h => (Hostname.build(h)))



  implicit val vclRequestReads = (
    ( JsPath \ "hostnames").read[Seq[Either[HostnameError,Hostname]]] and
    ( JsPath \ "ordered_rules").read[Seq[Either[RuleError,Rule]]] and
    ( JsPath \ "global_rules").read[Seq[Either[RuleError,Rule]]]
    )(VclRequest.build _)


  implicit val errorWrites: Writes[RuleError] = Json.writes[RuleError]
  implicit val hostnameErrorWrites = Json.writes[HostnameError]

  class ReadsWithRequiredArgs[A](delegate: Reads[A]) extends Reads[A] {
    def reads(json: JsValue) = {
      try delegate.reads(json) catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }
  }
}

