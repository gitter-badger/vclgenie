## Read below for different ways to contribute to the VCLgenie core 


<br/>

## Add a condition

If you want to add a new request condition , follow these steps 

#####1. Add a new condition to VclConfigCondition.scala 

 For example if we wanted to add a custom condtion that checked User-Agent we would first have to add a new VclCondition.   VclCondition accepts 4 parameters
 <br/>
 
 * key - the key that is used in the API to lookup the condition 
 * label - the label we use when displaying the form
 * conditionType - One of ValCond , NameValCond, DropDown.  Only ValCond and NameValCond are supported and the first accepts just a value while the latter accepts name and value
 * vclMatchers - The types of comparison operators supported (Equals,Matches, etc)
 
 If for example, we wanted to add a new condition to check on the user-agnet, it would looke like the following 
 
 ```
 val userAgent =  VclCondition("user_agent","User-Agent",ValCond,Seq(Equals,DoesNotEqual,Matches,DoesNotMatch) )
 ```
 And then we need to update conditionMap so we can look up this new key, by adding a new Key/Value to the map
 
 ```
 "user_agent" -> userAgent
 ```

#####2.  Update VCLHelpers.scala 

Now that we have a new request condition, we need to execute the VCL when this condition matches.  We do this in VCLHelpers.scala.

Look for the method vclCondition and add a new case pattern to match.  For our user-agent example, we would add a new case match that looks like this

```
case VCLConfigCondition.user_agent => 
  "(" + opToText("req.http.User-Agent",rulecondition.matcher.get,rulecondition.value) + ")"
```  

With a value of say "chrome" and a matcher of say "matches" this would spit out VCL that looks like 

```
   req.http.User-Agent ~ "chrome";   
```   

## Add an Action 

Adding an action is almost identical to adding a condition.  Follow these steps

#####1.  Add a new action to VclConfigAction.scala

Similar to conditions, we need to add a new val defining your action as well as add it to the actionMap.  VclConfigActions take several parameters

 * key - the key that is used in the API to lookup the action 
 * label - the label we use when displaying the form
 * actionType - The type of action, supported types are Bool (does not need a value, like Deny Request), SingleAction (can not add other actions), NameValAction (name and val required), Units (requires a units field for time)
 * vclFunctions - the vclFunction where this action can be placed.  Sometimes we need to stick a rule in multiple places, so this captures WHERE in the VCL we need to put the condition/action combination
 
#####2. Update VCLHelpers.scala

Again, we simply need to take action here in VCLHelpers, to ensure we can translate our new action to VCL.  Find the function vclAction and just like conditions, add another match to the case block.  


## Add more validations 

Adding validations is pretty straightforward.  Open ModelValidations.scala and add any new methods that you want.  Then, in either Rule.scala, RuleAction.scala or RuleCondition.scala, add your validation to the Seq() in the build() or apply() methods.

There is an implicit class "toValidate()" that takes a parameter and spits back the error if the result of the implicit class paramater is false.

## JSON Validation
There is a fair amount of "lifting" that happens when going from the JSON API to an actual case class that contains the conditions and actions.  There is also VERY heavy use of Either[X,XError] style format for consistency.  This was the cleanest way i could come up with a mechanism to define a way for the building of a condition/action/rule to either succeed or fail.  Then in validations we can easily check with pattern matching or map over  (_.right), etc. 

The basic JSON API request is VCLRequest class, in JsonAPI.scala.  This takes 4 parameters as defined (hostnames, ordered_rules, global_rules, backends).

VclRequestReads (in Formats.scala) then tries to build a case class for each of these , or return a Left() for any ones that either fail validation or are not valid JSON.

When building a "Rule" for global and ordered, we have to build RuleConditions and RuleActions, that themselves get built from VclConfigCondition and VclConfigAction respectively.  This allows us to go from a form with some name/value data to a proper statically typed instance of a class that we can use to generate VCL.  This should hopefully provide a LOT less errors, if we get our validations right :)