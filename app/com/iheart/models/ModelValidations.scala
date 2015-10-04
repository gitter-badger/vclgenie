package com.iheart.models

import com.iheart.models.VclConfigCondition._
import com.iheart.models.VclConfigAction._
import com.iheart.util.VclUtils._
import com.iheart.util.VclUtils.VclActionType._
import com.iheart.util.VclUtils.VclConditionType._
import play.Logger


trait ModelValidations extends ModelHelpers {

  import RegexUtils._

  def isValid(validations: Seq[Validation]): Either[Seq[ValidationError],Boolean]  = {

    def process(validations: Seq[Validation], errors: Seq[ValidationError] = Seq()): Seq[ValidationError]  = validations match {
      case h :: t if h.isRight => process(t,errors)
      case h :: t if h.isLeft => process(t,  errors :+ h.left.getOrElse("Validation error"))
      case _ => errors
    }

    process(validations) match {
      case Seq() => Right(true)
      case x => Left(x)
    }
  }

  def validCondition(key: String): Validation =
    conditionMap.get(key).isDefined.toValidate("Invalid condition key " + key)

  def validNetwork(acl: String): Boolean = {
    val pattern = "(\\d\\.\\d\\.\\d\\.\\d)/(\\d+)".r
    val net = acl match {
      case x if x.contains("/") => x
      case x => x + "/32"
    }
    pattern matches net
  }

  //Make sure network syntax is right 1.1.1.1/24
  def validAcl(key: String, value: String): Validation =
    (!(conditionMap.get(key).isDefined && conditionMap(key) == clientIp && !validNetwork(value))).toValidate("Invalid acl " + value)

  //Make sure NameVal cond has both name and val
  def validateNameValCondition(conditions: Seq[RuleCondition]): Validation =
    (conditions.count(c => c.condition.conditionType == NameValCond && (c.name.isEmpty || c.value.isEmpty)) == 0).toValidate("NameVal conditions must have name and value")

  //Make sure we used a valid matcher
  def validMatcher(key: String): Validation =
    vclMatcherMap.get(key).isDefined.toValidate("Invalid matcher " + key + " specified")

  //Make sure this matcher can be used for this Condition
  def validMatcherForCondition(key: String, m: String) = {
    val cond = conditionMap.get(key)
    val matcher = vclMatcherMap.get(m)

    (cond,matcher) match {
      case (Some(c), Some(mat)) =>
        c.vclMatchers.contains(mat._1).toValidate("You can not use matcher " + m + " with condition " + key)
      case _ => Right(true)
    }
  }

  //Validate the action name
  def validateAction(key: String): Validation =
    actionMap.get(key).isDefined.toValidate("Invalid action key of " + key)

  //Ensure there is only 1 action declared as SingleVal
  def validateSingleAction(actions: Seq[RuleAction]): Validation =
    (actions.count(a => a.action.actionType == SingleAction) < 2).toValidate("Only a single action of type SingleAction is permitted")

  //validate the matchType
  def validateMatchType(m: String): Validation =
    (m.toUpperCase == "ANY" || m.toUpperCase == "ALL").toValidate("Invalid matcher type of " + m)

  //Ensure NameVal action has name and val populated
  def validateNameValAction(actions: Seq[RuleAction]) =
    (actions.count(a => a.action.actionType == NameValAction && (a.name.isEmpty || a.value.isEmpty)) == 0).toValidate("NameVal actions must have name and value")

  //Make sure value is populated
  def validateValAction(actions: Seq[RuleAction]) =
    (actions.count(a => a.action.actionType == ValAction && a.value.isEmpty) == 0).toValidate("actions of type ValAction must have a value")

  //Make sure we use only a supported UNIT type
  def validateUnits(units: Option[String]) = (!(units.isDefined && vclUnitMap.get(units.get.toLowerCase).isEmpty )).toValidate("Invalid units " + units.getOrElse(""))

  //Make sure ordered rules have an index
  def hasIndex(rules: Seq[Either[RuleError,Rule]]) = {
    val valid = rules.count(r => r.isRight)
    (rules.count(r => r.isRight && r.right.get.index.isDefined) == valid).toValidate("All ordered rules must have an index field")
  }

  //If we reference a backend, make sure we declared it
  def validateBackendAction(rules: Seq[Either[RuleError,Rule]], backends: Seq[Either[BackendError,Backend]]): Validation = {
    val backendNames: Seq[String] = backends.filter(_.isRight).map(_.right.get.name)
    val referencedBackends = rules.filter(_.isRight).flatMap(_.right.get.actions.filter(_.action == setBackend)).map(_.value)

    (referencedBackends.count(name => name.isDefined && !backendNames.contains(name.getOrElse(None))) == 0)
       .toValidate("Invalid backend name specified")
  }

  def validateBackend(name: String, host: String) =
     true.toValidate("This can never be false :)")

  // We need at least 1 backend
  def validateBackendExists(backends: Seq[Either[BackendError,Backend]]) = (backends.size > 0).toValidate("At least one backend is required")
}
