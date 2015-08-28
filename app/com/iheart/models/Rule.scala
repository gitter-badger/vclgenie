package com.iheart.models

import com.iheart.json.Formats._
import com.iheart.models.VclConfigCondition._
import com.iheart.models.VclConfigAction._
import com.iheart.util.VclUtils.VclFunctionType.VclFunctionType
import com.iheart.util.VclUtils.VclMatchType
import com.iheart.util.VclUtils.VclMatchType
import com.iheart.util.VclUtils.VclMatchType.VclMatchType
import play.Logger

case class Rule(conditions: Seq[RuleCondition],
                actions: Seq[RuleAction],
                matchType: VclMatchType,
                index: Option[Int] ,
                id: String = java.util.UUID.randomUUID.toString) {

  def needsAcl = this.conditions.count(c => c.condition == clientIp) > 0

  def toConfigAction(str: String) = actionMap(str)
  
  def toConfigCondition(str: String) = conditionMap(str)

  def actionHasVclFunction(func: VclFunctionType): Boolean = actions.count(a => a.action.vclFunctions.contains(func)) > 0

}

object Rule extends ModelValidations {

  def hasError[RuleError,T](coll: Seq[Either[RuleError,T]]): Boolean = coll.count(c => c.isLeft) > 0

  def build(conditions: Seq[Either[RuleError,RuleCondition]],
            actions: Seq[Either[RuleError, RuleAction]],
            matchType: String,
            index: Option[Int]): Either[RuleError,Rule] = {
    if (hasError(conditions) || hasError(actions)) {
      Logger.info("RULE ERRORS: " + getErrors(conditions))
      Left(RuleError(getErrors(conditions) ++ getErrors(actions)))
    }

    else {
      isValid(Seq(validateMatchType(matchType),
                  validateSingleAction(actions.map(_.right.get)),
                  validateNameValAction(actions.map(_.right.get)),
                  validateNameValCondition(conditions.map(_.right.get)),
                  validateNameAction(actions.map(_.right.get)),
                  validateBoolAction(actions.map(_.right.get))) ) match {
        case Left(x) => Logger.info("error with rule: " + x.toString()); Left(RuleError(x))
        case Right(y) => Right (Rule(conditions.map (_.right.get), actions.map (_.right.get), VclMatchType.fromString(matchType), index) )
      }
    }

  }

}



