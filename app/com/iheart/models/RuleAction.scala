package com.iheart.models

import com.iheart.models.VclConfigAction._
import com.iheart.util.VclUtils.VclUnits.VclUnits
import com.iheart.util.VclUtils._

//                         set_ttl                           300s
case class RuleAction(action: VclAction, name: Option[String], value: Option[String], units: Option[VclUnits])

object RuleAction extends ModelValidations {
  def apply(key: String, name: Option[String], value: Option[String], units: Option[String]): Either[RuleError,RuleAction] = {
      isValid(Seq(validateAction(key),validateUnits(units))) match {
        case Left(x) => Left(RuleError(x))
        case Right(y)=>  Right(RuleAction(actionMap(key),name,value,vclUnitMap.get(units.getOrElse(""))))
      }
  }

  def build(key: String, name: Option[String], value: Option[String], units: Option[String]) =
    apply(key,name,value,units)
}