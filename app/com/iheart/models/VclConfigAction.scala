package com.iheart.models

import com.iheart.util.VclUtils.VclActionType._
import com.iheart.util.VclUtils.VclFunctionType._


case class VclAction(key: String, label: String, actionType: VclActionType,
                     validVclFunctions: Seq[VclFunctionType] )

object VclConfigAction {

  val doNotCache = VclAction("do_not_cache","Do Not Cache", Bool,Seq(vclBackendResp, vclHit))
  val setTTL = VclAction("set_ttl","Cache For", Units, Seq(vclBackendResp) )
  val httpRedirect = VclAction("http_redirect","HTTP Redirect to", SingleAction,  Seq(vclRecv,vclBackendResp,vclDeliver))
  val addCookie = VclAction("add_cookie","Add Cookie", NameValAction, Seq(vclDeliver) )
  val remCookies = VclAction("remove_cookie","Remove Cookies", Bool, Seq(vclRecv, vclBackendResp) )
  val denyRequest = VclAction("deny_request","Deny Request", Bool, Seq(vclRecv, vclBackendResp))
  val removeReqHeader = VclAction("remove_request_header","Remove Request Header", ValAction, Seq(vclRecv,vclBackendResp,vclDeliver,vclHit))
  val removeRespHeader = VclAction("remove_response_header","Remove Response Header", ValAction, Seq(vclBackendResp))
  val addReqHeader = VclAction("add_request_header","Add Request Header", NameValAction, Seq(vclRecv,vclBackendResp,vclDeliver,vclHit))
  val addRespHeader = VclAction("add_response_header","Add Response Header", NameValAction, Seq(vclDeliver))
  val setBackend = VclAction("set_backend","Set backend", ValAction,Seq(vclRecv))

  val actionMap = Map(
    "do_not_cache" -> doNotCache,
    "set_ttl" -> setTTL,
    "http_redirect" -> httpRedirect,
    "add_cookie" -> addCookie,
    "remove_cookies" -> remCookies,
    "deny_request" -> denyRequest,
    "remove_request_header" -> removeReqHeader,
    "remove_response_header" -> removeRespHeader,
    "add_request_header" -> addReqHeader,
    "add_response_header" -> addRespHeader,
    "set_backend" -> setBackend
  )
}
