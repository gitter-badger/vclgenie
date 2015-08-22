package com.iheart.models

import com.iheart.json.Formats._
import com.iheart.models.VclConfigCondition._
import com.iheart.models.VclConfigAction._
import play.Logger

case class Rule(conditions: Seq[RuleCondition], actions: Seq[RuleAction], matchType: String) {

  def needsAcl = ??? //conditions.count(c => c. == "needsACL") > 0

  def toConfigAction(str: String) = actionMap(str)
  
  def toConfigCondition(str: String) = conditionMap(str)

}

object Rule extends ModelValidations {

  def hasError[RuleError,T](coll: Seq[Either[RuleError,T]]): Boolean = coll.count(c => c.isLeft) > 0

  def getErrors[S <: RuleError,T](coll: Seq[Either[S,T]]): Seq[String] = coll.filter(c => c.isLeft).flatMap(c => c.left.get.errors)

  def build(conditions: Seq[Either[RuleError,RuleCondition]], actions: Seq[Either[RuleError, RuleAction]], matchType: String): Either[RuleError,Rule] = {
    if (hasError(conditions) || hasError(actions))
      Left(RuleError(getErrors(conditions) ++ getErrors(actions)))
    else {
      isValid(Seq(validMatchType(matchType),
                  validateSingleAction(actions.map(_.right.get)),
                  validateNameValAction(actions.map(_.right.get)),
                  validateNameAction(actions.map(_.right.get)),
                  validateBoolAction(actions.map(_.right.get)) )) match {
        case Left(x) => Logger.info("error with rule: " + x.toString()); Left(RuleError(x))
        case Right(y) => Right (Rule(conditions.map (_.right.get), actions.map (_.right.get), matchType) )
      }
    }

  }

}



