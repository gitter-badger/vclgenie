package com.iheart.models

import com.iheart.json.Formats.RuleError
import com.iheart.models.VclConfigAction._

//                         set_ttl                           300s
case class RuleAction(action: VclAction, name: Option[String], value: Option[String])

object RuleAction extends ModelValidations {
  def apply(key: String, name: Option[String], value: Option[String]): Either[RuleError,RuleAction] = {
      isValid(Seq(validAction(key))) match {
        case Left(x) => Left(RuleError(x))
        case Right(y)=>  Right(RuleAction(actionMap(key),name,value))
      }
  }

  def build(key: String, name: Option[String], value: Option[String]) =
    apply(key,name,value)
}