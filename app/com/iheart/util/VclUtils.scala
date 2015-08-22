package com.iheart.util

import com.iheart.models.VclAction
import com.iheart.models.VclConfigAction._

object VclUtils {

  object VclFunctionType extends Enumeration {
    type VclFunctionType = Value
    val vclFetch, vclRecv, vclDeliver = Value
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
    val Contains, DoesNotContain, Matches, DoesNotMatch, Equals, DoesNotEqual = Value
  }

  object VclMatchType extends Enumeration {
    type VclMatchType = Value
    val ANY, ALL = Value
  }

  import com.iheart.util.VclUtils.VclMatchers._

  val vclMatcherMap: Map[String,(VclMatchers.Value,String)] = Map(
    "contains" -> (Contains,"Contains"),
    "does_not_contain" -> (DoesNotContain,"Does Not Contain"),
    "matches" -> (Matches,"Matches"),
    "does_not_match" -> (DoesNotMatch,"Does Not Match"),
    "equals" -> (Equals,"Equals"),
    "does_not_equal" -> (DoesNotEqual,"Does Not Equal")
  )

  val vclMatcherReverseMap = Map(
     Contains -> "contains",
     DoesNotContain -> "does_not_contain",
     Matches -> "matches",
     DoesNotMatch -> "does_not_match",
     Equals -> "equals",
     DoesNotEqual -> "does_not_equal"
  )

}
