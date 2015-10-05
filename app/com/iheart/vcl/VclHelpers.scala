package com.iheart.vcl

import com.iheart.models._
import com.iheart.util.VclUtils.VclFunctionType.VclFunctionType
import com.iheart.util.VclUtils.VclMatchers.VclMatchers
import com.iheart.util.VclUtils.VclMatchers._
import com.iheart.util.VclUtils.{VclMatchType, VclFunctionType,VclUnits}
import com.iheart.util.VclUtils.VclFunctionType._
import com.iheart.util.VclUtils.VclUnits._
import com.iheart.models.VclConfigCondition
import com.iheart.models.VclConfigCondition._
import com.iheart.models.VclConfigAction
import com.iheart.models.VclConfigAction._

import play.api.Logger



trait VCLHelpers {

  var globalConfig: String = ""
  var vclBackendRespStr: String = ""
  var vclDeliverStr: String = ""
  var vclRecvStr: String = ""
  var vclErrorStr: String = ""


  //***************************************************************************
  // HELPER FUNCTIONS
  //***************************************************************************

  def addTabs(tabs: Int):String = {
    def acc(s: String, tabs:Int): String = tabs match {
      case 0 => s
      case _ => acc(s + "\t",tabs-1)
    }
    acc("",tabs)
  }

  def addComment(tabs: Int, s: String) = {
    globalConfig +=  "\n#----------------------------------------\n"
    globalConfig += "#" + addTabs(1) + s + "\n"
    globalConfig +=  "#----------------------------------------\n"
  }

  def addToVcl(str: String, vclFunc: VclFunctionType) = vclFunc match {
    case VclFunctionType.`vclBackendResp` => vclBackendRespStr += str
    case VclFunctionType.vclRecv => vclRecvStr += str
    case VclFunctionType.vclDeliver => vclDeliverStr += str
    case VclFunctionType.vclError => vclErrorStr += str
  }

  def generateAcl(rules: Seq[Rule]) = {

    val pattern = "(\\d\\.\\d\\.\\d\\.\\d)/(\\d+)".r

    rules.filter(_.needsAcl).foreach { rule =>

      globalConfig += "acl acl_" + rule.id + "{ \n"
      val aclRules = rule.conditions.filter(f => f.condition == VclConfigCondition.clientIp )

      aclRules.foreach { aclrule =>
        val net = aclrule.value match {
          case x if x.contains("/") => x
          case x => x + "/32"
        }
        val pattern(ip,mask) = net
        globalConfig += addTabs(1) + "\"" + ip + "\"" + "/" +   mask + " ;\n"
      }
      globalConfig += "}\n\n"
    }
  }


  def generateBackends(b: Seq[Backend]) = {
      def generateBackend(backend: Backend) = {
        globalConfig += "backend " + backend.name + " { \n"
        globalConfig += addTabs(1) + ".host = \"" + backend.host + "\" ;\n"
        globalConfig += addTabs(1) + ".host_header = \"" + backend.hostHeader + "\" ; \n"
        globalConfig += addTabs(1) + ".port = \"" + backend.port + "\"; \n"
        if (backend.probe.isDefined)
          globalConfig += addTabs(1) + ".probe = healthcheck; \n\n"
        globalConfig += "}\n\n"
      }

     b.map(backend => generateBackend(backend))

     addToVcl("set req.backend_hint = " + b.head.name + ";", vclRecv)
  }

  def toTTL(units: VclUnits) = units match {
    case VclUnits.DAYS => "d"
    case VclUnits.HOURS => "h"
    case VclUnits.MINUTES => "m"
    case VclUnits.SECONDS => "s"
    case VclUnits.YEARS => "y"
  }

  def toNetwork(s: String) = s match {
    case x if x.contains("/") => x
    case x => x + "/32"
  }


  def vclAction(ruleaction: RuleAction, vclFunction: VclFunctionType) = {

    val reqHeader = vclFunction match {
      case VclFunctionType.vclBackendResp => "bereq"
      case _ => "req"
    }

    val respHeader = vclFunction match {
      case VclFunctionType.vclBackendResp => "beresp"
      case _ => "resp"
    }

    val actionStr = ruleaction.action match  {
      case VclConfigAction.doNotCache =>
        "set beresp.ttl = 0s; "
      case VclConfigAction.setTTL =>
       s"""set beresp.ttl = ${ruleaction.value.get}${toTTL(ruleaction.units.getOrElse(VclUnits.SECONDS))};"""
      case VclConfigAction.httpRedirect =>
        s"""return(synth(799,"${ruleaction.value.get}"));"""
      case VclConfigAction.denyRequest =>
        " return(synth(403));"
      case VclConfigAction.removeReqHeader  =>
        s"""unset $reqHeader.http.${ruleaction.value.get};"""
      case VclConfigAction.removeRespHeader =>
        s"""unset beresp.http.${ruleaction.value.get};"""
      case VclConfigAction.addReqHeader  =>
        s"""set $reqHeader.http.${ruleaction.name.get} = "${ruleaction.value.get}" ;"""
      case VclConfigAction.addRespHeader =>
         s"""set resp.http.${ruleaction.name.get} = "${ruleaction.value.get}" ;"""
      case VclConfigAction.remCookies  =>
        s"""unset $respHeader.http.cookie ;"""
      case VclConfigAction.setBackend =>
        "set req.backend_hint = " + ruleaction.value.get + ";"
      case _ => ""
    }

    addTabs(2) + actionStr
  }

  def opToText(field: String, op: VclMatchers, value: String, quotes: Boolean = true ) =  {
    val quoteChar = if (quotes)
      "\""
    else
      ""

    op match {
      case Equals => s"$field == $quoteChar$value$quoteChar"
      case DoesNotEqual => s"$field != $quoteChar$value$quoteChar"
      case Matches => s"$field ~ $quoteChar$value$quoteChar"
      case DoesNotMatch =>  s"$field !~ $quoteChar$value$quoteChar"
      case GreaterThen => s"$field > $value"
      case LessThen => s"$field < $value"
      case StartsWith => s"$field ~ $quoteChar^$value$quoteChar"
      case x => "UNKNOWN OP " + x
    }
  }

  def toAcl(rule: Rule) = "acl_" + rule.id.toString

  def vclCondition(rule: Rule, rulecondition: RuleCondition, vclFunction: VclFunctionType) = rulecondition.condition match {
    case VclConfigCondition.requestUrl if vclFunction == vclBackendResp =>
      " ( " + opToText("bereq.url",rulecondition.matcher.get,rulecondition.value) + " ) "
    case VclConfigCondition.requestUrl  =>
      " ( " + opToText("req.url",rulecondition.matcher.get,rulecondition.value) + " ) "
    case VclConfigCondition.contentType if vclFunction == vclBackendResp =>
      val contentTypes = rulecondition.value.split(",").map(u => u.trim).mkString("|")
      " ( " + opToText("bereq.http.Content-Type",rulecondition.matcher.get,contentTypes) + " ) "
    case VclConfigCondition.contentType =>
      val contentTypes = rulecondition.value.split(",").map(u => u.trim).mkString("|")
      " ( " + opToText("req.http.Content-Type",rulecondition.matcher.get,contentTypes) + " ) "
    case VclConfigCondition.clientIp =>
      " ( " + opToText("client.ip",rulecondition.matcher.get, toAcl(rule),false) + " ) "
    case VclConfigCondition.requestParam  if vclFunction == vclBackendResp =>
      " ( " + opToText("bereq.url",rulecondition.matcher.get,s"${rulecondition.name.get}=${rulecondition.value}") + " ) "
    case VclConfigCondition.requestParam =>
      " ( " + opToText("req.url",rulecondition.matcher.get,s"${rulecondition.name.get}=${rulecondition.value}") + " ) "
    case VclConfigCondition.clientCookie if vclFunction == vclBackendResp  =>
      val str = s"""header.get(bereq.http.cookie,"${rulecondition.name.get} = ${rulecondition.value}")"""
      " ( " + opToText(str,rulecondition.matcher.get,"^$") + " ) "
    case VclConfigCondition.clientCookie =>
      val str = s"""header.get(req.http.cookie,"${rulecondition.name.get} = ${rulecondition.value}")"""
      " ( " + opToText(str,rulecondition.matcher.get,"^$") + " ) "
    case VclConfigCondition.requestHeader if vclFunction == vclBackendResp=>
      " ( " + opToText("bereq.http." + rulecondition.name.get, rulecondition.matcher.get, rulecondition.value) + " ) "
    case VclConfigCondition.requestHeader =>
      " ( " + opToText("req.http." + rulecondition.name.get, rulecondition.matcher.get, rulecondition.value) + " ) "
    case VclConfigCondition.fileExtension if vclFunction == vclBackendResp =>
      " ( " + opToText("bereq.http.ext", rulecondition.matcher.get, rulecondition.value) + " ) "
    case VclConfigCondition.fileExtension =>
      " ( " + opToText("req.http.ext", rulecondition.matcher.get, rulecondition.value) + " ) "
  }


 // ****************************************************
 //  HOSTNAME FUNCTIONS
 // ****************************************************

  def generateHostConditions(hostnames: Seq[Hostname], ruleset: String) = {
    val funcs = List(vclBackendResp, vclRecv, vclDeliver)

    funcs.foreach { vclfunc =>
      var block = "\n"
      if (vclfunc == vclBackendResp)
        block +=  addTabs(1) + " if (" + hostnames.map( h => "(bereq.http.Host == \"" + h.name + "\")").mkString("||") + ") {\n"
      else
        block +=  addTabs(1) + " if (" + hostnames.map( h => "(req.http.Host == \"" + h.name + "\")").mkString("||") + ") {\n"
      block += addTabs(2) + "call ruleset_" + ruleset + "_global_" + vclfunc + ";\n"
      block += addTabs(2) + "call ruleset_" + ruleset + "_ordered_" + vclfunc + ";\n"
      block += addTabs(1) + " }\n"
      addToVcl(block,vclfunc)
    }
  }

  //***************************************************
  //  GENERIC FUNCTIONS
  //***************************************************



  def closeConfigs = {
    vclBackendRespStr += "}\n"
    globalConfig += vclBackendRespStr

    vclRecvStr += addTabs(1) + "else { return(synth(403)); } \n"
    vclRecvStr += addTabs(1) + " call cleanup_request_headers;\n"
    vclRecvStr += "}\n"
    globalConfig += vclRecvStr

    vclDeliverStr += addTabs(1) + "call cleanup_response_headers; \n"
    vclDeliverStr += "}\n"
    globalConfig += vclDeliverStr

    globalConfig += vclErrorStr

  }

  val baseVcl = """
                  |  vcl 4.0;
                  |
                  |  import std;
                  |  import header;
                  |
                  |
                  |
                  |  #----------------------------------------
                  |  # System Wide Subroutines
                  |  #----------------------------------------
                  |  sub cleanup_response_headers {
                  |    unset resp.http.X-Varnish;
                  |  }
                  |
                  |  sub cleanup_request_headers {
                  |    unset req.http.ext ;
                  |  }
                  |
                  |

                  |"""
    .stripMargin

  vclDeliverStr =
    """
      |#------------------------
      |# VCL_DELIVER
      |#------------------------
      |sub vcl_deliver {
      |
      |  if (obj.hits > 0) {
      |     set resp.http.X-Cache = "HIT";
      |     } else {
      |     set resp.http.X-Cache = "MISS";
      |  }
      |
    """.stripMargin

  vclBackendRespStr =
    """
      |#------------------------
      |# VCL_BACKEND_RESPONSE
      |#------------------------
      |sub vcl_backend_response {
    """.stripMargin

  vclRecvStr =
    """
      |#------------------------
      |# VCL_RECV
      |#------------------------
      |sub vcl_recv {
      |
      |
      |     set req.http.ext = regsub( req.url, "\\?.+$", "" );
      |     set req.http.ext = regsub( req.http.ext, ".+\\.([a-zA-Z0-9]+)$", "\\1" );
      |
    """.stripMargin

  vclErrorStr =
    """
      |#------------------------
      |# VCL_ERROR
      |#------------------------
      |sub vcl_synth {
      |
      |    call cleanup_request_headers;
      |
      |    if (resp.status == 799) {
      |      set resp.http.Location = resp.reason;
      |      set resp.status = 302;
      |      return(deliver);
      |    }
      |}
      |
    """.stripMargin
}
