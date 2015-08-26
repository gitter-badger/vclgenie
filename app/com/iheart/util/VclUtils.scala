package com.iheart.util

import com.iheart.models.VclAction
import com.iheart.models.VclConfigAction._

object VclUtils {

  object VclUnits extends Enumeration {
    type VclUnits = Value
    val SECONDS, MINUTES, HOURS, DAYS, WEEKS, YEARS = Value
  }

  object VclFunctionType extends Enumeration {
    type VclFunctionType = Value
    val vclFetch = Value("vcl_fetch")
    val vclRecv = Value("vcl_recv")
    val vclDeliver = Value("vcl_deliver")
    val vclError = Value("vcl_error")
    val vclHit = Value("vcl_hit")
    val vclMiss = Value("vcl_miss")

  }

  object VclActionType extends Enumeration {
    type VclActionType = Value
    val Bool, SingleAction, NameValAction, NameAction, Units = Value
  }

  object VclConditionType extends Enumeration {
    type VclConditionType = Value
    val SingleCond, NameValCond, Dropdown = Value
  }

  object VclMatchers extends Enumeration {
    type VclMatchers = Value
    val Matches, DoesNotMatch, Equals, DoesNotEqual = Value
  }

  object VclMatchType extends Enumeration {

    type VclMatchType = Value
    val ANY, ALL = Value

    def fromString(s: String) = s match {
      case "ALL" => ALL
      case "ANY" => ANY
    }
  }

  import com.iheart.util.VclUtils.VclMatchers._

  val vclMatcherMap: Map[String,(VclMatchers.Value,String)] = Map(
    "matches" -> (Matches,"Matches"),
    "does_not_match" -> (DoesNotMatch,"Does Not Match"),
    "equals" -> (Equals,"Equals"),
    "does_not_equal" -> (DoesNotEqual,"Does Not Equal")
  )

  val vclMatcherReverseMap = Map(
     Matches -> "matches",
     DoesNotMatch -> "does_not_match",
     Equals -> "equals",
     DoesNotEqual -> "does_not_equal"
  )

  val vclUnitMap = Map(
    "seconds" -> VclUnits.SECONDS,
    "minutes" -> VclUnits.MINUTES,
    "hours" -> VclUnits.HOURS,
    "days" -> VclUnits.DAYS,
    "weeks" -> VclUnits.WEEKS,
    "years" -> VclUnits.YEARS
  )

}
