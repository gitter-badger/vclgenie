#### VCLGenie

VclGenie is an API (with an angular frontend if desired) to essentially turn a JSON API request into Varnish VCL.  The API accepts 4 parameters, defined more below.

#API

See the tests for examples of the JSON API.  The format of the JSON API is  

>     {
>   	 "ordered_rules" :
>                     [
>                        {
>                           "conditions" : [ ],
>                           "actions" : [ ],
>                           "match_type" : "ANY | ALL",
>                           "index" : 
>                        },                       
>                      ],
> 
>       "global_rules" : [
>                         {
>                           "conditions" : [ ],
>                           "actions" : [ ],
>                           "match_type" : "ANY | ALL",
>                         },                       
> 		                ],                                   
>       "hostnames" :
>                    [ 
>                       { 
>                          "hostname" : }, 
>                    ] ,
>       "backends" : [ 
>                      { "name" : "backend1" , 
>                        "host" : "www.myhost.com",                                                              							"host_header" : "www.myhost.com"
>                       } 
>                    ]
>   	 }

#Rules

 There are two type of rules, ordered rules and global rules.  Ordered rules have an index, and are executed in an if/else block such that the first rule that matches gets executed and then stops.  Global rules are ALL executed on EVERY request (if the condition matches).  Global rules are useful when you want to universally apply some action to every request, like add a request header, etc.
 
#Conditions and Actions

Conditions and actions each have different "types" as explained below.  Different types require certain fields (like name or value ) to be populated depending on the type.  For example, an http redirect only requires a value (the URL to redirect to), whereas adding an HTTP Response header, requires a name and value.  The table below outlines which fields are required for each type of condition and action.

If you would like to see the restrictions, check out ModelValidations.scala for a snapshot of some of the things we check against for the different types of requests (I am sure plenty are missing, pull requests welcome :) ).

###Conditions 

The currently support list of conditions are 

* Request URL  - Match on anything in the URL 
* Content Type - Match on the Content Type header
* Client IP - Do an ACL based match on an IP Network (only ipv4 supported currently)
* Request Param - Match on a query string param
* Client Cookie - Match on a cooke name/value 
* Request Header - Match on an HTTP Request header
* File Extension - Match on the file extension of the request 

The below table outlines what is required in the JSON.  The "key" field is the value of the condition in the JSON, and there is an X in the conditions that require either name, value or both.  The matcher column outlines which matchers are supported.  

| Condition     | key         | name   | value    | 
|---------------|-------------|:------:|:--------:|
|Request Url    | request_url |        |  x       |
|Content Type   | content_type |       |  x       |
|Client IP      | client_ip    |       |  x       |
|Request Param  | request_param | x    |  x       |
|Cookie         | client_cookie | x    |  x       |
|Request Header | request_header | x   |  x       |
|File Extension | file_extension | x   |  x       |

 The format of the condition in the JSON API is 

    { "condition" : key, 
      "name" : "somename",
      "value" : "somevalue",
      "matcher" : "equals | does_not_equal | matches | does_not_match" 
    }
