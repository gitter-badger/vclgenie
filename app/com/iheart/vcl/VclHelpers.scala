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
  var vclFetchStr: String = ""
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
    case VclFunctionType.vclFetch => vclFetchStr += str
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

  //***************************************************************************
  // BACKEND/DIRECTOR FUNCTIONS
  //***************************************************************************

//  def printRRDirector(h: Hostname)(backends: List[String]) = {
//    globalConfig += "director director_" + h._id.stringify + " round-robin {" + "\n"
//    backends.map { b =>
//      globalConfig += addTabs(1) + "{ .backend = " + b + " ;} \n"
//    }
//    globalConfig += "}\n\n"
//  }
//
//  def printBackend(backend: String, ip: String, h: Hostname) = {
//    globalConfig += "backend " + backend + " { \n"
//    globalConfig += addTabs(1) + ".host = \"" + ip + "\" ;\n"
//    globalConfig += addTabs(1) + ".host_header = \"" + h.name + "\" ; \n"
//    globalConfig += addTabs(1) + ".port = \"80\"; \n"
//    globalConfig += addTabs(1) + ".probe = healthcheck; \n\n"
//    globalConfig += "}\n\n"
//  }
//
//
//  def backendName(h: Hostname, ip: String) = "backend_" + h._id.stringify + "_" + ip.replace(".","_")
//
//  def generateBackend(h: Hostname): Unit = {
//
//    Logger.info("Printing backend for hostname" + h.name)
//    val printRR = printRRDirector(h) _
//
//    def backAcc(ips: List[String], backends: List[String]): List[String] = ips match {
//      case Nil => backends
//      case _ => {
//        printBackend(backendName(h,ips.head),ips.head,h)
//        backAcc(ips.tail,backends :+ backendName(h,ips.head))
//      }
//    }
//
//    addComment(1, "HOSTNAME " + h.name)
//    printRR(backAcc(h.validOriginIps,List()))
//  }


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

     addToVcl("set req.backend = " + b.head.name + ";", vclRecv)
  }

  def toTTL(units: VclUnits) = units match {
    case VclUnits.DAYS => "d"
    case VclUnits.HOURS => "h"
    case VclUnits.MINUTES => "m"
    case VclUnits.SECONDS => "s"
  }

  def toNetwork(s: String) = s match {
    case x if x.contains("/") => x
    case x => x + "/32"
  }


  def vclAction(ruleaction: RuleAction, vclFunction: VclFunctionType) = ruleaction.action match  {
    case VclConfigAction.doNotCache => "set beresp.ttl = 0s; "
    case VclConfigAction.setTTL => addTabs(2) + "set beresp.ttl = " + ruleaction.value.get + toTTL(ruleaction.units.getOrElse(VclUnits.SECONDS)) + ";"
    case VclConfigAction.httpRedirect=> addTabs(2) + "error 799 " + ruleaction.value.get + " ;"
    case VclConfigAction.denyRequest => addTabs(2) +"error 403;"
    case VclConfigAction.removeReqHeader => addTabs(2) +"unset req.http." + ruleaction.value + ";"
    case VclConfigAction.removeRespHeader => addTabs(2) +"unset resp.http." + ruleaction.value + ";"
    case VclConfigAction.addReqHeader => addTabs(2) +"set req.http." + ruleaction.name.get + " = " + ruleaction.value.get + ";"
    case VclConfigAction.addRespHeader => addTabs(2) + s"""set resp.http.${ruleaction.name.get} = "${ruleaction.value.get}" ;"""
    case VclConfigAction.remCookies if vclFunction == vclFetch =>  addTabs(2) +"unset beresp.http.cookie ;"
    case VclConfigAction.remCookies if vclFunction == vclRecv =>  addTabs(2) + "unset resp.http.cookie;"
    case VclConfigAction.setBackend => addTabs(2) + "set req.backend = " + ruleaction.name.get + ";"
    case _ => ""
  }

  def opToText(field: String, op: VclMatchers, value: String, quotes: Boolean = true ) =  {
    val quoteChar = if (quotes == true)
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

  def vclCondition(rule: Rule, rulecondition: RuleCondition) = rulecondition.condition match {
    case VclConfigCondition.requestUrl =>
      val urls = rulecondition.value.split(",").map(u => u.trim).mkString("|")
      " ( " + opToText("req.url",rulecondition.matcher.get,urls) + " ) "
    case VclConfigCondition.contentType =>
      val contentTypes = rulecondition.value.split(",").map(u => u.trim).mkString("|")
      " ( " + opToText("req.http.ext",rulecondition.matcher.get,contentTypes) + " ) "
    case VclConfigCondition.clientIp => " ( " + opToText("client.ip",rulecondition.matcher.get, toAcl(rule),false) + " ) "
    case VclConfigCondition.requestParam => " ( " + opToText("req.url",rulecondition.matcher.get,s"${rulecondition.name.get}=${rulecondition.value}") + " ) "
    case VclConfigCondition.clientCookie =>
      val str = s"""header.get(req.http.cookie,"${rulecondition.name.get} = ${rulecondition.value}")"""
      " ( " + opToText(str,rulecondition.matcher.get,"^$") + " ) "
    case VclConfigCondition.requestHeader =>
      " ( " + opToText("req.http." + rulecondition.name.get, rulecondition.matcher.get, rulecondition.value) + " ) "
    //case "country" => " ( " + opToText("req.http.X-GeoIP", rulecondition.matcher, rulecondition.value) + " ) "
  }




 // ****************************************************
 //  HOSTNAME FUNCTIONS
 // ****************************************************

  def generateHostConditions(hostnames: Seq[Hostname], ruleset: String) = {
    val funcs = List(vclFetch, vclRecv, vclDeliver)

    funcs.foreach { vclfunc =>
      var block = "\n"
      block +=  addTabs(1) + " if (" + hostnames.map( h => "(req.http.Host == \"" + h.name + "\")").mkString("||") + ") {\n"
      if (vclfunc == VclFunctionType.vclRecv)
        block += addTabs(2) + s"""set req.backend = director_${ruleset} ; \n """
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
    vclFetchStr += "}\n"
    globalConfig += vclFetchStr

    vclRecvStr += addTabs(1) + "else { error 403; } \n"
    vclRecvStr += addTabs(1) + " call cleanup_request_headers;\n"
    vclRecvStr += "}\n"
    globalConfig += vclRecvStr

    vclDeliverStr += addTabs(1) + "call cleanup_response_headers; \n"
    vclDeliverStr += "}\n"
    globalConfig += vclDeliverStr

    globalConfig += vclErrorStr

  }

  val baseVcl = """

                  |  import std;
                  |  import header;
                  |  import digest;
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

  vclFetchStr =
    """
      |#------------------------
      |# VCL_FETCH
      |#------------------------
      |sub vcl_fetch {
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
      |sub vcl_error {
      |
      |    call cleanup_request_headers;
      |
      |    if (obj.status == 799) {
      |      set obj.http.Location = obj.response;
      |      set obj.status = 302;
      |      return(deliver);
      |    }
      |}
      |
    """.stripMargin
}
