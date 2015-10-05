## Read below for different ways to contribute to the VCLgenie core 


<br/><br/>

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