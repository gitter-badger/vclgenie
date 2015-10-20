package com.iheart.models

import com.iheart.models.VclConfigCondition._
import com.iheart.util.VclUtils.VclMatchers.VclMatchers
import com.iheart.util.VclUtils.VclMatchers._
import com.iheart.util.VclUtils._
import play.Logger

case class RuleCondition(condition: VclCondition, matcher: Option[VclMatchers], value: String, name: Option[String] = None)

object RuleCondition extends ModelValidations {
  def apply(key: String, matcher: String, value: String, name: Option[String]): Either[RuleError,RuleCondition] = {
    isValid(Seq(validCondition(key),validMatcher(key,matcher),validAcl(key,value))) match {
      case Left(x) => Logger.info("Error: " + x); Left(RuleError(x))
      case Right(y) =>  Right(RuleCondition(conditionMap(key), vclMatcherMap.get(matcher).map(_._1), value, name))
    }
  }

  def build(key: String, matcher: Option[String], value: Option[String], name: Option[String]) =
     apply(key,matcher.getOrElse(""),value.getOrElse(""),name)
}
