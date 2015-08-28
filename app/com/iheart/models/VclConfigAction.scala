package com.iheart.models

import com.iheart.util.VclUtils.VclActionType._
import com.iheart.util.VclUtils.VclFunctionType._


case class VclAction(label: String, actionType: VclActionType,
                     vclFunctions: Seq[VclFunctionType] )

object VclConfigAction {

  val doNotCache = VclAction("Do Not Cache", Bool,Seq(vclFetch))
  val setTTL = VclAction("Cache For", Units, Seq(vclFetch) )
  val httpRedirect = VclAction("HTTP Redirect to", SingleAction,  Seq(vclRecv))
  val addCookie = VclAction("Add Cookie", NameValAction, Seq(vclDeliver) )
  val remCookies = VclAction("Remove Cookies", Bool, Seq(vclRecv, vclFetch) )
  val denyRequest = VclAction("Deny Request", Bool, Seq(vclRecv))
  val removeReqHeader = VclAction("Remove Request Header", NameAction, Seq(vclRecv))
  val removeRespHeader = VclAction("Remove Response Header", NameAction, Seq(vclFetch))
  val addReqHeader = VclAction("Add Request Header", NameValAction, Seq(vclRecv))
  val addRespHeader = VclAction("Add Response Header", NameValAction, Seq(vclFetch))
  val setBackend = VclAction("Set backend", NameAction,Seq(vclRecv,vclFetch))

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
