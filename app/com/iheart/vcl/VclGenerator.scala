package com.iheart.vcl

import com.iheart.models.{Backend, Hostname, VclConfigCondition, Rule}
import com.iheart.util.VclUtils.VclFunctionType._
import com.iheart.util.VclUtils.VclMatchType
import com.iheart.util.VclUtils._
import play.Logger


class VclGenerator extends VCLHelpers {


  def parseGlobalRule(rule: Rule, vclFunction: VclFunctionType) = {
    val actions = rule.actions.filter(a => a.action.validVclFunctions.contains(vclFunction)).map { action =>
      vclAction(action,vclFunction)
    }

    val conditions = rule.conditions.map { condition =>
      vclCondition(rule,condition,vclFunction)
    }

    val conditional = if (rule.matchType == VclMatchType.ALL) "&&"
    else "||"

    globalConfig += addTabs(1) + "if ( "
    globalConfig += conditions.mkString(conditional)
    globalConfig += " ) { \n"
    globalConfig += actions.mkString("\n")
    globalConfig += "\n" + addTabs(1) + "}\n"
  }

  def parseGlobalRules(ruleset: String, rules: Seq[Rule]) = {
    val funcs = List(vclBackendResp,vclRecv,vclDeliver, vclHit)

    funcs.foreach { vclfunc =>
      globalConfig += "sub ruleset_" + ruleset +  "_global_" + vclfunc.toString + " { \n"
      rules.filter(_.actionHasVclFunction(vclfunc)).foreach { rule =>
        parseGlobalRule(rule,vclfunc)
      }
      globalConfig += "}\n\n"
    }
  }

  def parseOrderedRule(rule: Rule, vclFunction: VclFunctionType, idx: Int) = {

    val conditional = if (rule.matchType == VclMatchType.ALL) "&&"
    else "||"

    val ruleIf = if (idx > 0)
      "else if ( "
    else  "if ( "

    val actions = rule.actions.map { action =>
      vclAction(action,vclFunction)
    }

    val conditions = rule.conditions.map { condition =>
      vclCondition(rule,condition,vclFunction)
    }

    globalConfig += addTabs(1) + ruleIf
    globalConfig += conditions.mkString(conditional)
    globalConfig += " ) { \n"
    globalConfig += actions.mkString("\n")
    globalConfig += "\n" + addTabs(1) + "}\n"
  }

  //  def parseOrderedRuleAcc(rules: List[Rule], vclFunction: String, ruleNum: Int = 0, orderedVCL: String = "" ): String = rules match {
  //    case h :: t =>
  //    case Nil => orderedVCL
  //  }

  def parseOrderedRules(ruleset: String, rules: Seq[Rule]) = {
    //val funcs = List(vclBackendResp,vclRecv,vclDeliver, vclHit)
    val funcs: Seq[VclFunctionType] =
      rules.flatMap(r => r.actions.map(a => a.action.validVclFunctions)).flatten intersect
       rules.flatMap(r => r.conditions.map(c => c.condition.validVclFunctions)).flatten

    funcs.map { vclfunc =>
      globalConfig += "sub ruleset_" + ruleset + "_ordered_" + vclfunc + " { \n"
      rules.filter(_.actionHasVclFunction(vclfunc)).sortBy(_.index).zipWithIndex.foreach { case (rule,idx) =>
        parseOrderedRule(rule,vclfunc,idx)
      }
      globalConfig +=  "}\n\n"  //End sub for this VCL Function
    }
  }

  def generateRuleset(hostnames: Seq[Hostname],
                      orderedRules: Seq[Rule],
                      globalRules: Seq[Rule],
                      backends: Seq[Backend],
                      ruleset: String = generateUUID): String = {
    globalConfig = baseVcl
    generateAcl(orderedRules ++ globalRules)
    generateBackends(backends)
    generateHostConditions(hostnames,ruleset)
    addComment(1,"Global Rules")
    parseGlobalRules(ruleset,globalRules)
    addComment(1,"Ordered Rules")
    parseOrderedRules(ruleset,orderedRules)
    closeConfigs
    globalConfig
  }



}
