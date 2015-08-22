package com.iheart.vcl

import com.iheart.models._
import com.iheart.util.VclUtils.VclFunctionType.VclFunctionType
import com.iheart.util.VclUtils.VclMatchers.VclMatchers
import com.iheart.util.VclUtils.VclMatchers._
import com.iheart.util.VclUtils.{VclMatchType, VclFunctionType}
import com.iheart.util.VclUtils.VclFunctionType._
import com.iheart.models.VclConfigCondition
import com.iheart.models.VclConfigCondition._
import com.iheart.models.VclConfigAction
import com.iheart.models.VclConfigAction._

import play.api.Logger



trait VCLHelpers {

  var globalConfig: String
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

  def addToVcl(str: String, vclFunc: String) = vclFunc match {
    case "vcl_fetch" => vclFetchStr += str
    case "vcl_recv" => vclRecvStr += str
    case "vcl_deliver" => vclDeliverStr += str
    case "vcl_error" => vclErrorStr += str
  }

  //***************************************************************************
  // BACKEND/DIRECTOR FUNCTIONS
  //***************************************************************************

  def printRRDirector(h: Hostname)(backends: List[String]) = {
    globalConfig += "director director_" + h._id.stringify + " round-robin {" + "\n"
    backends.map { b =>
      globalConfig += addTabs(1) + "{ .backend = " + b + " ;} \n"
    }
    globalConfig += "}\n\n"
  }

  def printBackend(backend: String, ip: String, h: Hostname) = {
    globalConfig += "backend " + backend + " { \n"
    globalConfig += addTabs(1) + ".host = \"" + ip + "\" ;\n"
    globalConfig += addTabs(1) + ".host_header = \"" + h.name + "\" ; \n"
    globalConfig += addTabs(1) + ".port = \"80\"; \n"
    globalConfig += addTabs(1) + ".probe = healthcheck; \n\n"
    globalConfig += "}\n\n"
  }


  def backendName(h: Hostname, ip: String) = "backend_" + h._id.stringify + "_" + ip.replace(".","_")

  def generateBackend(h: Hostname): Unit = {

    Logger.info("Printing backend for hostname" + h.name)
    val printRR = printRRDirector(h) _

    def backAcc(ips: List[String], backends: List[String]): List[String] = ips match {
      case Nil => backends
      case _ => {
        printBackend(backendName(h,ips.head),ips.head,h)
        backAcc(ips.tail,backends :+ backendName(h,ips.head))
      }
    }

    addComment(1, "HOSTNAME " + h.name)
    printRR(backAcc(h.validOriginIps,List()))
  }

  //------------------------------------------------------------
  // RULESET FUNCTIONS
  //------------------------------------------------------------

  def generateAcl(rules: Seq[Rule]) = {

    val pattern = "(\\d\\.\\d\\.\\d\\.\\d)/(\\d+)".r

    rules.filter(_.needsAcl).map { rule =>

      globalConfig += "acl rule_" + rule._id.stringify + "{ \n"

      rule.conditions.filter(_.key == "client_ip").map { aclrule =>
        val net = aclrule.value match {
          case x if x.contains("/") => x
          case x => x + "/32"
        }
        /*    val ip = new SubnetUtils(net).getInfo
           globalConfig += addTabs(1) + "\"" + ip.getNetworkAddress + "\""
           if (ip.getNetmask != "255.255.255.255")
             globalConfig += "/" + ip.getNetmask */
        val pattern(ip,mask) = net
        globalConfig += addTabs(1) + "\"" + ip + "\"" + "/" +   mask + " ;\n"
      }
      globalConfig += "}\n\n";
    }
  }

  def toTTL(units: String) = units match {
    case "DAYS" => "d"
    case "HOURS" => "h"
    case "MINUTES" => "m"
    case "SECONDS" => "s"
  }


  def vclAction(ruleaction: RuleAction, vclFunction: VclFunctionType) = ruleaction.action match  {
    case VclConfigAction.doNotCache => "set beresp.ttl = 0s; "
    case VclConfigAction.setTTL => addTabs(2) + "set beresp.ttl = " + ruleaction.value.get + toTTL(action.units.getOrElse("SECONDS")) + ";\n"
    case VclConfigAction.httpRedirect=> "error 799 " + ruleaction.value + " ;"
    case VclConfigAction.denyRequest => "error 403;"
    case VclConfigAction.removeReqHeader => "unset req.http." + ruleaction.value + ";"
    case VclConfigAction.removeRespHeader => "unset resp.http." + ruleaction.value + ";"
    case VclConfigAction.addReqHeader => "set req.http." + ruleaction.name + " = " + ruleaction.value + ";"
    case VclConfigAction.addRespHeader => "set resp.http." + ruleaction.name + " = " + ruleaction.value + ";"
    case VclConfigAction.remCookies if vclFunction == vclFetch =>  "unset beresp.http.cookie ;"
    case VclConfigAction.remCookies if vclFunction == vclRecv =>  "unset resp.http.cookie;"
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
      case Contains => s"$field ~ $quoteChar$value$quoteChar"
      case DoesNotMatch =>  s"$field !~ $quoteChar$value$quoteChar"
      case DoesNotContain => s"$field !~ $quoteChar$value$quoteChar"
//      case "greaterthan" => s"$field > $value"
//      case "lessthan" => s"$field < $value"
//      case "startswith" => s"$field ~ $quoteChar^$value$quoteChar"
      case x => "UNKNOWN OP " + x
    }
  }

  def vclCondition(rule: Rule, rulecondition: RuleCondition) = rulecondition.condition.get match {
    case VclConfigCondition.requestUrl =>
      val urls = rulecondition.value.split(",").map(u => u.trim).mkString("|")
      " ( " + opToText("req.url",rulecondition.matcher.get,urls) + " ) "
    case VclConfigCondition.contentType =>
      val contentTypes = rulecondition.value.split(",").map(u => u.trim).mkString("|")
      " ( " + opToText("req.http.ext",rulecondition.matcher.get,contentTypes) + " ) "
    case VclConfigCondition.clientIp => " ( " + opToText("client.ip",rulecondition.matcher.get,"rule_" + rule._id.stringify,false) + " ) "
    case VclConfigCondition.requestParam => " ( " + opToText("req.url",rulecondition.matcher.get,s"$rulecondition.conditionType.name=$condition.value") + " ) "
    case VclConfigCondition.clientCookie =>
      val str = s"""header.get(req.http.cookie,"${rulecondition.name.get} = ${rulecondition.value}")"""
      " ( " + opToText(str,rulecondition.matcher.get,"^$") + " ) "
    case VclConfigCondition.requestHeader => " ( " + opToText("req.http." + rulecondition.name.get, rulecondition.matcher.get, rulecondition.value) + " ) "
    //case "country" => " ( " + opToText("req.http.X-GeoIP", rulecondition.matcher, rulecondition.value) + " ) "
  }

  def parseGlobalRule(rule: Rule, vclFunction: VclFunctionType) = {
    val actions = rule.actions.map { action =>
      vclAction(action,vclFunction)
    }

    val conditions = rule.conditions.map { condition =>
      vclCondition(rule,condition)
    }

    val conditional = if (rule.matchType == VclMatchType.ALL) "&&"
    else "||"

    globalConfig += addTabs(1) + "if ( "
    globalConfig += conditions.mkString(conditional)
    globalConfig += " ) { \n"
    globalConfig += actions.mkString("\n")
    globalConfig += addTabs(1) + "}\n"
  }

  def parseGlobalRules(rules: Seq[Rule]) = {
    val funcs = List(vclFetch,vclRecv,vclDeliver)

    funcs.foreach { vclfunc =>
      globalConfig += "sub ruleset_" + "_global_" + vclfunc + " { \n"
      rules.filter(_.actions.contains(vclfunc)).foreach { rule =>
        parseGlobalRule(rule,vclfunc)
      }
      globalConfig += "}\n\n"
    }
  }

  def parseOrderedRule(rule: Rule, vclFunction: String, idx: Int) = {

    val conditional = if (rule.matchType == VclMatchType.ALL) "&&"
    else "||"

    val ruleIf = if (idx > 0)
      "else if ( "
    else  "if ( "

    val actions = rule.actions.map { action =>
      vclAction(action,vclFunction)
    }

    val conditions = rule.conditions.map { condition =>
      vclCondition(rule,condition)
    }

    globalConfig += addTabs(1) + ruleIf
    globalConfig += conditions.mkString(conditional)
    globalConfig += " ) { \n"
    globalConfig += actions.mkString("\n")
    globalConfig += addTabs(1) + "}\n"
  }

  //  def parseOrderedRuleAcc(rules: List[Rule], vclFunction: String, ruleNum: Int = 0, orderedVCL: String = "" ): String = rules match {
  //    case h :: t =>
  //    case Nil => orderedVCL
  //  }

  def parseOrderedRules(rules: Seq[Rule]) = {
    val funcs = List(VCLFETCH, VCLRECV, VCLDELIVER)

    funcs.map { vclfunc =>
      globalConfig += "sub ruleset_" + ruleset._id.stringify + "_ordered_" + vclfunc + " { \n"
      rules.sortBy(_.sortIndex).filter(_.vclFunctions.contains(vclfunc)).zipWithIndex.map { case (rule,idx) =>
        parseOrderedRule(rule,vclfunc,idx)
      }
      globalConfig +=  "}\n\n"  //End sub for this VCL Function
    }
  }


  //****************************************************
  // HOSTNAME FUNCTIONS
  //****************************************************

  def generateHostCondition(hostname: Hostname, vclFunction: String, idx: Int) = {
    val ruleIf = if (idx > 0)
      "else if ( "
    else  "if ( "

    var block = "\n "

    block += s"""${ruleIf} req.http.Host == "${hostname.name}" ) { \n """
    if (vclFunction == "vcl_recv")
      block += s""" set req.backend = director_${hostname._id.stringify} ; \n """
    block += addTabs(1) + "call ruleset_" + hostname.ruleset.get._id.stringify + "_global_" + vclFunction + ";\n"
    block += addTabs(1) + "call ruleset_" + hostname.ruleset.get._id.stringify + "_ordered_" + vclFunction + ";\n"
    block += " }\n"
    addToVcl(block,vclFunction)
  }

  def generateHostConditions(hostnames: List[Hostname]) = {
    val funcs = List(VCLFETCH, VCLRECV, VCLDELIVER)

    funcs.map { vclfunc =>
      hostnames.zipWithIndex.map { case (hostname,idx) =>
        generateHostCondition(hostname,vclfunc,idx)
      }
    }
  }

  //***************************************************
  //  GENERIC FUNCTIONS
  //***************************************************

  def generateRuleset(rules: Seq[Rule]) = {
    generateAcl(rules)
    parseGlobalRules(rules)
    parseOrderedRules(rules)
  }

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

  }

  val baseVcl = """

                  |  import std;
                  |  import ssutil;
                  |  import geoip;
                  |  import header;
                  |  import digest;
                  |
                  |
                  |  #----------------------------------------
                  |  # System Wide Subroutines
                  |  #----------------------------------------
                  |  sub cleanup_response_headers {
                  |    unset resp.http.X-SS-PURGEURL;
                  |    unset resp.http.X-SS-PURGEHOST;
                  |    unset resp.http.X-Varnish;
                  |  }
                  |
                  |  sub cleanup_request_headers {
                  |    unset req.http.X-SS-ClientIP ;
                  |    unset req.http.X-SS-RequestStart ;
                  |    unset req.http.X-SS-Expiration;
                  |    unset req.http.X-SS-Token;
                  |    unset req.http.X-SS-Key;
                  |    unset req.http.ext ;
                  |    unset req.http.X-GeoIP-Client;
                  |    unset req.http.X-GeoIP;
                  |    unset req.http.X-SS-Key;
                  |  }
                  |
                  |  sub set_geoip {
                  |    set req.http.X-GeoIP-Client = client.ip;
                  |    set req.http.X-GeoIP = geoip.country(req.http.X-GeoIP-Client);
                  |  }
                  |
                  |   sub validate_token_url {
                  |
                  |     set req.http.X-SS-Token = regsuball(req.url,".*[?&]ss_token=([^&]+).*","\\1");
                  |     set req.http.X-SS-Expiration = regsuball(req.url,".*[?&]ss_expiration=([^&]+).*","\\1");
                  |
                  |     std.syslog(1,"EXPIRATION IS " + req.http.X-SS-Expiration);
                  |
                  |     set req.http.X-SS-Expiration2 = req.http.X-SS-Expiration;
                  |     set req.http.X-SS-Token2 = req.http.X-SS-Token;
                  |
                  |     if (ssutil.time_expired(req.http.X-SS-Expiration) < 0) {
                  |       error 403 "Token is expired" ;
                  |     }
                  |
                  |     set req.url = regsuball(req.url,"\\?ss_token=[^&]+$",""); # strips when QS = "?sstoken=AAA"
                  |     set req.url = regsuball(req.url,"\\?ss_token=[^&]+&","?"); # strips when QS = "?sstoken=AAA&foo=bar"
                  |     set req.url = regsuball(req.url,"&ss_token=[^&]+",""); # strips when QS = "?foo=bar&sstoken=AAA" or QS = "?foo=bar&sstoken=AAA&bar=baz"
                  |
                  |     set req.url = regsuball(req.url,"\\?ss_expiration=[^&]+$",""); # strips when QS = "?sstoken=AAA"
                  |     set req.url = regsuball(req.url,"\\?ss_expiration=[^&]+&","?"); # strips when QS = "?sstoken=AAA&foo=bar"
                  |     set req.url = regsuball(req.url,"&ss_expiration=[^&]+",""); # strips when QS = "?foo=bar&sstoken=AAA" or QS = "?foo=bar&sstoken=AAA&bar=baz"
                  |
                  |     if (req.http.X-SS-Token != digest.hash_md5(req.http.X-SS-Key + digest.hash_md5(req.http.X-SS-Expiration + req.url + req.http.X-SS-Key ) )  ) {
                  |        error 403 "Invalid Token";
                  |      }
                  |
                  |     unset req.http.X-SS-Key ;
                  |     unset req.http.X-SS-Token ;
                  |     unset req.http.X-SS-Expiration ;
                  |
                  |  }
                  |
                  | sub validate_token_header {
                  |
                  |     if (ssutil.time_expired(req.http.X-SS-Expiration) < 0) {
                  |       error 403 "Token is expired" ;
                  |     }
                  |
                  |     set req.http.X-Debug-Token = digest.hash_md5(req.http.X-SS-Key + digest.hash_md5(req.http.X-SS-Expiration + req.url + req.http.X-SS-Key ) ) ;
                  |     if (req.http.X-SS-Token != digest.hash_md5(req.http.X-SS-Key + digest.hash_md5(req.http.X-SS-Expiration + req.url + req.http.X-SS-Key ) ) ) {
                  |        error 403 "Invalid Token in Header";
                  |     }
                  |
                  |     unset req.http.X-SS-Key ;
                  |     unset req.http.X-SS-Token ;
                  |     unset req.http.X-SS-Expiration ;
                  |
                  |  }
                  |
                  |  sub validate_token_cookie {
                  |
                  |     set req.http.X-SS-Token = regsub( req.http.Cookie, "^.*?ss_token=([^;]*);*.*$", "\1" );
                  |     set req.http.X-SS-Expiration = regsub( req.http.Cookie, "^.*?ss_expiration=([^;]*);*.*$", "\1" );
                  |
                  |     unset req.http.Cookie ;
                  |
                  |     if (ssutil.time_expired(req.http.X-SS-Expiration) < 0) {
                  |       error 403 "Token is expired" ;
                  |     }
                  |
                  |     if (req.http.X-SS-Token != digest.hash_md5(req.http.X-SS-Key + digest.hash_md5(req.http.X-SS-Expiration + req.url + req.http.X-SS-Key ) ) ) {
                  |        error 403 "Invalid Token in Cookie";
                  |     }
                  |
                  |     unset req.http.X-SS-Key;
                  |     unset req.http.X-SS-Token;
                  |     unset req.http.X-SS-Expiration;
                  |
                  |  }
                  |
                  |
                  |  probe healthcheck {
                  |   .url = "/scalesimple/index.html";
                  |   .interval = 5s;
                  |   .timeout = 0.3 s;
                  |   .window = 8;
                  |   .threshold = 3;
                  |   .initial = 3;
                  |   .expected_response = 200;
                  |  }
                  |
                  |  sub vcl_init {
                  |     #geoip.init_database("/usr/local/share/GeoIP/GeoLiteCity.dat");
                  |  }
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
      |     set resp.http.X-SS-Cache = "HIT";
      |     } else {
      |     set resp.http.X-SS-Cache = "MISS";
      |  }
      |
      |  set resp.http.X-SS-Expiration2 = req.http.X-SS-Expiration2;
      |  set resp.http.X-SS-Token2 = req.http.X-SS-Token2;
    """.stripMargin

  vclFetchStr =
    """
      |#------------------------
      |# VCL_FETCH
      |#------------------------
      |sub vcl_fetch {
      |
      |  set beresp.http.X-SS-PURGEURL = req.url;
      |  set beresp.http.X-SS-PURGEHOST = req.http.host ;
    """.stripMargin

  vclRecvStr =
    """
      |#------------------------
      |# VCL_RECV
      |#------------------------
      |sub vcl_recv {
      |
      |   # This is completely lame and just so the VCL compiler
      |   # doesnt bitch at unused functions
      |   if (false) {
      |    call validate_token_header;
      |    call validate_token_url;
      |    call validate_token_cookie;
      |    call set_geoip;
      |   }
      |
      |   set req.http.X-SS-ClientIP = client.ip ;
      |   set req.http.X-SS-RequestStart = ssutil.time_str();
      |   set req.http.ext = regsub( req.url, "\\?.+$", "" );
      |   set req.http.ext = regsub( req.http.ext, ".+\\.([a-zA-Z0-9]+)$", "\\1" );
      |
      |   if (req.http.host ~ "#{$CNAME_TYPE[:test]}$") {
      |    set req.http.host = regsub(req.http.host,"^(.*)?\.#{$CNAME_TYPE[:test]}","\\1");
      |   }

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
