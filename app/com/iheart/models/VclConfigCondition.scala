package com.iheart.models

import com.iheart.util.VclUtils.VclConditionType._
import com.iheart.util.VclUtils.VclMatchers._


case class VclCondition( key: String,
                         label: String,
                        conditionType: VclConditionType,
                        vclMatchers: Seq[VclMatchers] )

object VclConfigCondition {

  val requestUrl = VclCondition("request_url","Request Url", ValCond,Seq(Equals,DoesNotEqual,Matches,DoesNotMatch))
  val contentType = VclCondition("content_type","Content Type", ValCond, Seq(Equals,DoesNotEqual,Matches,DoesNotMatch))
  val clientIp = VclCondition("client_ip","Client IP/Network", ValCond, Seq(Matches, DoesNotMatch))
  val requestParam = VclCondition("request_param","Request Parameter", NameValCond, Seq(Equals,DoesNotEqual,Matches,DoesNotMatch))
  val clientCookie = VclCondition("cookie","Cookie", NameValCond, Seq(Matches,DoesNotMatch,Equals,DoesNotEqual))
  val requestHeader = VclCondition("request_header","Request header", NameValCond, Seq(Matches,DoesNotMatch,Equals,DoesNotEqual))

  val conditionMap = Map(
    "request_url" -> requestUrl,
    "content_type" -> contentType,
    "client_ip" -> clientIp,
    "request_param" -> requestParam,
    "cookie" -> clientCookie,
    "request_header" -> requestHeader)

  val singleValConditions = conditionMap.filter(x => x._2.conditionType == ValCond)
  val nameValueConditions = conditionMap.filter(x => x._2.conditionType == NameValCond)
}
