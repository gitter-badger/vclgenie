package com.iheart.models

import com.iheart.util.VclUtils.VclConditionType._
import com.iheart.util.VclUtils.VclMatchers._


case class VclCondition(label: String,
                        conditionType: VclConditionType,
                        vclMatchers: Seq[VclMatchers] )

object VclConfigCondition {

  val requestUrl = VclCondition("Request Url", SingleCond,Seq(Contains,DoesNotContain))
  val contentType = VclCondition("Content Type", SingleCond, Seq(Contains,DoesNotContain,Matches,DoesNotMatch))
  val clientIp = VclCondition("Client IP/Network", SingleCond, Seq(Matches, DoesNotMatch))
  val requestParam = VclCondition("Request Parameter", NameValCond, Seq(Equals,DoesNotEqual,Matches,DoesNotMatch))
  val clientCookie = VclCondition("Cookie", NameValCond, Seq(Matches,DoesNotMatch,Equals,DoesNotEqual))
  val requestHeader = VclCondition("Request header", NameValCond, Seq(Matches,DoesNotMatch,Equals,DoesNotEqual))

  val conditionMap = Map(
    "request_url" -> requestUrl,
    "content_type" -> contentType,
    "client_ip" -> clientIp,
    "request_param" -> requestParam,
    "cookie" -> clientCookie,
    "request_header" -> requestHeader)

  val singleValConditions = conditionMap.filter(x => x._2.conditionType == SingleCond)
  val nameValueConditions = conditionMap.filter(x => x._2.conditionType == NameValCond)
}
