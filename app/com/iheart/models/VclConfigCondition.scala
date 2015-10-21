package com.iheart.models

import com.iheart.util.VclUtils.VclConditionType._
import com.iheart.util.VclUtils.VclFunctionType.VclFunctionType
import com.iheart.util.VclUtils.VclFunctionType._
import com.iheart.util.VclUtils.VclMatchers._


case class VclCondition( key: String,
                         label: String,
                        conditionType: VclConditionType,
                        vclMatchers: Seq[VclMatchers] ,
                        validVclFunctions: Seq[VclFunctionType] )

object VclConfigCondition {

  val requestUrl = VclCondition("request_url","Request Url", ValCond,Seq(Equals,DoesNotEqual,Matches,DoesNotMatch), Seq(vclRecv,vclBackendResp,vclDeliver,vclHit))
  val contentType = VclCondition("content_type","Content Type", ValCond, Seq(Equals,DoesNotEqual,Matches,DoesNotMatch),Seq(vclRecv,vclBackendResp,vclDeliver,vclHit))
  val clientIp = VclCondition("client_ip","Client IP/Network", ValCond, Seq(Matches, DoesNotMatch), Seq(vclRecv))
  val requestParam = VclCondition("request_param","Request Parameter", NameValCond, Seq(Equals,DoesNotEqual,Matches,DoesNotMatch), Seq(vclRecv,vclBackendResp,vclDeliver,vclHit))
  val clientCookie = VclCondition("cookie","Cookie", NameValCond, Seq(Matches,DoesNotMatch,Equals,DoesNotEqual), Seq(vclRecv,vclBackendResp))
  val requestHeader = VclCondition("request_header","Request header", NameValCond, Seq(Matches,DoesNotMatch,Equals,DoesNotEqual), Seq(vclRecv,vclBackendResp,vclDeliver,vclHit))
  val fileExtension = VclCondition("file_extension", "File Extension", ValCond, Seq(Equals,DoesNotEqual,Matches,DoesNotMatch), Seq(vclRecv,vclBackendResp,vclDeliver,vclHit))
  val isCached = VclCondition("is_cached", "is Cached", BoolCond, Seq(), Seq(vclHit) )

  val conditionMap = Map(
    "request_url" -> requestUrl,
    "content_type" -> contentType,
    "client_ip" -> clientIp,
    "request_param" -> requestParam,
    "cookie" -> clientCookie,
    "request_header" -> requestHeader,
    "file_extension" -> fileExtension ,
    "is_cached" -> isCached )

  val singleValConditions = conditionMap.filter(x => x._2.conditionType == ValCond)
  val nameValueConditions = conditionMap.filter(x => x._2.conditionType == NameValCond)
  val boolConditions = conditionMap.filter(x => x._2.conditionType == BoolCond)
}
