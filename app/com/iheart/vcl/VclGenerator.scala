package com.iheart.vcl

import com.iheart.models.{Backend, Hostname, VclConfigCondition, Rule}
import com.iheart.util.VclUtils.VclFunctionType._
import com.iheart.util.VclUtils.VclMatchType
import com.iheart.util.VclUtils._
import play.Logger


class VclGenerator extends VCLHelpers {



  def generateGlobalRule(rule: Rule, vclFunction: VclFunctionType) = {
    val actions = rule.actions.filter(a => a.action.validVclFunctions.contains(vclFunction)).map { action =>
      vclAction(action,vclFunction)
    }

    val conditions = rule.conditions.map { condition =>
      vclCondition(rule,condition,vclFunction)
    }

    val conditional = if (rule.matchType == VclMatchType.ALL) "&&"
    else "||"

    addTabs(1) + "if ( " +
         conditions.mkString(conditional) +
         " ) { \n" +
         actions.mkString("\n") +
         "\n" + addTabs(1) + "}\n"
  }


  def generateGlobalRules(ruleset: String, rules: Seq[Rule])(config: VclOutput) = {
    val funcs = List(vclBackendResp,vclRecv,vclDeliver, vclHit)

    val res = funcs.map { vclfunc =>
      "sub ruleset_" + ruleset +  "_global_" + vclfunc.toString + " { \n" +
      rules.filter(_.actionHasVclFunction(vclfunc)).map { rule =>
        generateGlobalRule(rule,vclfunc)
      }.mkString +
      "}\n\n"
    }.mkString

    config.copy(globalConfig = config.globalConfig + res)
  }

  def generateOrderedRule(rule: Rule, vclFunction: VclFunctionType, idx: Int) = {

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

    addTabs(1) + ruleIf +
      conditions.mkString(conditional) +
      " ) { \n" +
      actions.mkString("\n") +
      "\n" + addTabs(1) + "}\n"
  }


  def generateOrderedRules(ruleset: String, rules: Seq[Rule])(config: VclOutput) = {
    val funcs: Seq[VclFunctionType] =
      rules.flatMap(r => r.actions.map(a => a.action.validVclFunctions)).flatten intersect
       rules.flatMap(r => r.conditions.map(c => c.condition.validVclFunctions)).flatten

    val res = funcs.map { vclfunc =>
      "sub ruleset_" + ruleset + "_ordered_" + vclfunc + " { \n" +
      rules.filter(_.actionHasVclFunction(vclfunc)).sortBy(_.index).zipWithIndex.map { case (rule,idx) =>
        generateOrderedRule(rule,vclfunc,idx)
      }.mkString +
      "}\n\n"  //End sub for this VCL Function
    }.mkString

    config.copy(globalConfig = config.globalConfig + res)
  }

  def generateRuleset(hostnames: Seq[Hostname],
                      orderedRules: Seq[Rule],
                      globalRules: Seq[Rule],
                      backends: Seq[Backend],
                      ruleset: String = generateUUID): String = {

    val config = VclOutput(globalConfig = baseVcl)

    val pipeline = Seq(generateAcl(orderedRules ++ globalRules) _,
                       generateBackends(backends) _ ,
                       generateHostConditions(hostnames,ruleset) _,
                       addComment(1,"Global Rules") _,
                       generateGlobalRules(ruleset,globalRules) _ ,
                       addComment(1,"Ordered Rules") _,
                       generateOrderedRules(ruleset,orderedRules) _ ,
                       closeConfigs _
    )

    val f = Function.chain(pipeline)
    f(config).globalConfig
  }



}
