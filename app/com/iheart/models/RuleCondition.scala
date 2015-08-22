package com.iheart.models

import com.iheart.json.Formats.RuleError
import com.iheart.models.VclConfigCondition._
import com.iheart.util.VclUtils.VclMatchers.VclMatchers
import com.iheart.util.VclUtils.VclMatchers._
import com.iheart.util.VclUtils._

//                                 request_url                                matches           someval             somecookiename
case class RuleCondition(condition: Option[VclCondition], matcher: Option[VclMatchers], value: String, name: Option[String] = None)  {
  require(condition.isDefined, "Invalid condition")
}

object RuleCondition extends ModelValidations {
  def apply(key: String, matcher: String, value: String, name: Option[String]): Either[RuleError,RuleCondition] = {
    isValid(Seq(validCondition(key),validMatcher(matcher))) match {
      case Left(x) => Left(RuleError(x))
      case Right(y) =>  Right(RuleCondition(conditionMap.get(key), vclMatcherMap.get(matcher).map(_._1), value, name))
    }
  }

  def build(key: String, matcher: String, value: String, name: Option[String]) =
     apply(key,matcher,value,name)

}
