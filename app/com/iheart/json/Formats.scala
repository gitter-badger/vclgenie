package com.iheart.json

import com.iheart.models._
import com.iheart.util.VclUtils.VclActionType.VclActionType
import com.iheart.util.VclUtils.VclConditionType._
import com.iheart.util.VclUtils.VclFunctionType._
import com.iheart.util.VclUtils.VclMatchers._
import com.iheart.util.VclUtils._
import com.iheart.models.VclConfigCondition._
import play.Logger
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.libs.Json._
import play.api.libs.functional.syntax._


object Formats {

  implicit val ruleConditionFormat: Reads[Either[RuleError,RuleCondition]] = (
    ( JsPath \ "condition").read[String] and
    (JsPath \ "matcher").readNullable[String] and
      (JsPath \ "value").readNullable[String] and
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

  implicit val backendFormat: Reads[Either[BackendError,Backend]] = (
    ( JsPath \ "name").read[String] and
    ( JsPath \ "host").read[String] and
    ( JsPath \ "host_header").read[String] and
    ( JsPath \ "port").readNullable[Int] and
    ( JsPath \ "probe").readNullable[String]
    )(Backend.build _)


  implicit val vclRequestReads = (
    ( JsPath \ "hostnames").read[Seq[Either[HostnameError,Hostname]]] and
    ( JsPath \ "ordered_rules").read[Seq[Either[RuleError,Rule]]] and
    ( JsPath \ "global_rules").read[Seq[Either[RuleError,Rule]]] and
    ( JsPath \ "backends").read[Seq[Either[BackendError,Backend]]]
    )(VclRequest.build _)


  implicit val ruleErrorWrites: Writes[RuleError] = Json.writes[RuleError]
  implicit val hostnameErrorWrites = Json.writes[HostnameError]
  implicit val backendErrorWrites = Json.writes[BackendError]

  class ReadsWithRequiredArgs[A](delegate: Reads[A]) extends Reads[A] {
    def reads(json: JsValue) = {
      try delegate.reads(json) catch {
        case e: IllegalArgumentException => JsError(e.getMessage)
      }
    }
  }




  case class ConfigResponse(nameValueConditions: Map[String,VclCondition],
                            singleValueConditions: Map[String,VclCondition],
                            boolConditions: Map[String,VclCondition],
                            actions: Map[String, VclAction])

  implicit val vclCondTypeWrites: Writes[VclConditionType] = new Writes[VclConditionType] {
    def writes(condType: VclConditionType) = {
      Json.toJson(condType.toString)
    }
  }

  implicit val vclMatcherWrites: Writes[VclMatchers] = new Writes[VclMatchers] {
    def writes(matcher: VclMatchers) = {
      val str = vclMatcherReverseMap(matcher)
      val toForm = vclMatcherMap(str)._2
      Json.obj("label" ->  toForm, "key" -> str )
    }
  }

  implicit val vclActionTypeWrites: Writes[VclActionType] = new Writes[VclActionType] {
    def writes(actionType: VclActionType) = {
      Json.toJson(actionType.toString)
    }
  }

  implicit val vclActionWrites: Writes[VclAction] = new Writes[VclAction] {
    def writes(a: VclAction) = {
      Json.obj("key" -> a.key,
               "label" -> a.label,
               "action_type" -> a.actionType,
               "vcl_functions" -> a.vclFunctions)
    }
  }

  implicit val vclCondWrites: Writes[VclCondition] = new Writes[VclCondition] {
     def writes(condition: VclCondition) = {
       Json.obj("key" -> condition.key,
                "label" -> condition.label,
                "condition_type" -> condition.conditionType,
                "vcl_matchers" -> condition.vclMatchers)
     }
  }

  implicit val crWrites: Writes[ConfigResponse] = new Writes[ConfigResponse] {
    def writes(cr: ConfigResponse): JsValue = {
      val nvc = cr.nameValueConditions.map( (nv) => Json.toJson(nv._2))
      val svc = cr.singleValueConditions.map( (sv) => Json.toJson(sv._2))
      val bvc = cr.boolConditions.map( (sv) => Json.toJson(sv._2))
      val actions = cr.actions.map( a => Json.toJson( a._2))
      val matchers = vclMatcherMap.map( v => Json.obj("key" -> v._1, "label" -> v._2._2))
      Json.obj("conditions" -> svc.++(nvc).++(bvc), "actions" -> actions, "vcl_matchers" -> matchers)
    }

  }
}

