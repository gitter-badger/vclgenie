#### VCLGenie

VclGenie is an API (with an angular frontend located in frontend/) to essentially turn a JSON API request into Varnish VCL.  The API accepts 4 parameters, defined more below.

This is basically an alpha version and there are very likely a lot of bugs and edge cases.  Dealing with nuances of VCL is not always straightforward :) 

If you are interested in contributing, please read [Contributing.md](Contributing.md)

#API

See the tests for examples of the JSON API.  The format of the JSON API is roughly something like this: 

```
     {
   	 "ordered_rules" :
                     [
                        {
                           "conditions" : [ ],
                           "actions" : [ ],
                           "match_type" : "ANY | ALL",
                           "index" : 
                        },                       
                      ],
 
       "global_rules" : [
                         {
                           "conditions" : [ ],
                           "actions" : [ ],
                           "match_type" : "ANY | ALL",
                         },                       
 		                ],                                   
       "hostnames" :   [ 
                         { "hostname" : "www.example1.com" },
                         { "hostname" : "www.example2.com" }
                       ] ,
       "backends" : [ 
                      { "name" : "backend1" , 
                        "host" : "www.myhost.com",                                                              						 "host_header" : "www.myhost.com",
                        "port" : 80 
                       } 
                    ]
   	 }
```   	 

#Rules

 There are two type of rules, ordered rules and global rules.  Ordered rules have an index, and are executed in an if/else block such that the first rule that matches gets executed and then stops.  Global rules are ALL executed on EVERY request (if the condition matches).  Global rules are useful when you want to universally apply some action to every request, like add a request header, etc.
 
#Conditions and Actions

Conditions and actions each have different "types" as explained below.  Different types require certain fields (like name or value ) to be populated depending on the type.  For example, an http redirect only requires a value (the URL to redirect to), whereas adding an HTTP Response header, requires a name and value.  The table below outlines which fields are required for each type of condition and action.

If you would like to see the restrictions, check out ModelValidations.scala for a snapshot of some of the things we check against for the different types of requests (I am sure plenty are missing, pull requests welcome :) ).

###Conditions 

The currently supported list of conditions are 

* Request URL  - Match on anything in the URL 
* Content Type - Match on the Content Type header
* Client IP - Do an ACL based match on an IP Network (only ipv4 supported currently)
* Request Param - Match on a query string param
* Client Cookie - Match on a cooke name/value 
* Request Header - Match on an HTTP Request header
* File Extension - Match on the file extension of the request 

The below table outlines what is required in the JSON.  The "key" field is the value of the condition in the JSON, and there is an X in the conditions that require either name, value or both.
The matchers column indicates which matchers are valid for that condition.  The supported list is E(Equals), DNE(Does Not Equal), M(Matches), DNM(Does Not Match)    

| Condition     | key         | name   | value    |matchers
|---------------|-------------|:------:|:--------:|:--------:|
|Request Url    | request_url |        |  x       |E, DNE, M, DNM 
|Content Type   | content_type |       |  x       |E, DNE, M, DNM
|Client IP      | client_ip    |       |  x       |M, DNM
|Request Param  | request_param | x    |  x       |E, DNE, M, DNM
|Cookie         | client_cookie | x    |  x       |E, DNE, M, DNM
|Request Header | request_header | x   |  x       |E, DNE, M, DNM
|File Extension | file_extension | x   |  x       |E, DNE, M, DNM

 The format of the condition in the JSON API is 

```
"conditions" : [ 
      { "condition" : key, 
       "name" : "somename",
       "value" : "somevalue",
       "matcher" : "equals | does_not_equal | matches | does_not_match" 
      }
 ]
``` 

As a quick example, if I was going to add 2 conditions, one for request url and one for request param, the conditions block in the JSON could look like this 

```
"conditions" : [ 
      { 
       "condition" : "request_url", 
       "value" : "/somepath",
       "matcher" : "equals" 
      },
      {
        "condition" : "request_param",
        "name" : "source",
        "value" : "rssfeed",
        "matcher" : "does_not_equal"
      }
 ]
```

### Actions

Actions are setup almost identically to Conditions, but have a slightly different set of types.  Additionally, we map every action to the VCL function that it needs, although knowledge of this is not necessary and is automatically managed in the backend API (Check VCLHelpers.scala if interested).

The currently supported list of actions are 

* Do Not Cache - Set TTL to 0 
* Set TTL - Set TTL to a specified value
* HTTP Redirect - Issue a 302 redirect
* Add Cookie - Add a Response Cookie
* Remove Cookies - Remove Cookies from the request and backend response
* Deny Request - Issue a 403 forbidden 
* Remove Request Header - Remove a header from the request
* Remove Response Header - Remove a response header from the origin
* Add Request Header - Add an HTTP Request header to the backend request
* Add Response Header - Add an HTTP request header to the client response
* Set Backend - Set the request to use a different backend (see BACKENDS)


The below table outlines what is required in the JSON.  The "key" field is the value of the action in the JSON, and there is an X in the conditions that require either name, value or units. NOTE, in some cases (like Do Not Cache), neither name, value or units fields are required since its a simple Boolean action type, meaning no value is needed, and just by using that key, the intent is known (issue a 403, for example).

----

NOTE: *In addition to simple name, value , actions can contain an extra type called "units".  Currently this is only for set_ttl, but i am sure further uses can be found.  The only supported values for the "units" field are SECONDS, MINUTES, HOURS, DAYS, WEEKS, YEARS.*

----

| Action        | key         | name   | value    | units  |
|---------------|-------------|:------:|:--------:|:------:|
|Do Not Cache   | do_not_cache|        |          |        |
|Set TTL        | set_ttl     |        |   x      |   x    |
|HTTP Redirect  | http_redirect |      |   x      |        |
|Add Cookie     | add_cookie  |  x     |   x      |        |
|Remove Cookies | remove_cookie|       |          |        |
|Deny Request   | deny_request |       |          |        |
|Remove Req Header| remove_req_header|  |   x     |        |
|Remove Resp Header| remove_resp_header | | x     |        |
|Add Req Header | add_request_header | x |  x     |        |
|Add Resp Header| add_response_header | x | x     |        |
|Set Backend | set_backend |  | x |  | 

Using another action example, if we wanted to use 2 actions, Set TTL, and add Response Header, the JSON could look like this 

```
  "actions" : [
     { 
       "action" : "set_ttl",
       "value" : 300 ,
       "units" : "SECONDS"
     },
     {
       "action" : "add_response_header",
       "name" : "X-HTTP-MyHeader",
       "value" : "customData"
     }
  ]
```


### Hostnames

  Hostnames right now are pretty simple. We simply OR all the hostnames provided.  In a future release we may provide more per host granularity in terms of which rules get bound to each hostname.  For right now, its pretty basic.
  
  The API required field for hostnames is "hostname". Thats it :) 

```
  "hostnames" : [
     {
        "hostname" : "www.example.com"
     }
  ]  
```   
  
### Backends

  VCL requires at least one backend, so we do as well.  We also let you name your backend, so you can reference it in your actions.  For example, you can set 2 backends (we use the first as the default in vcl_recv) and then in an action rule later on, you can set a different backend (by name) based on a rule.  
  
  The required fields for backends is "name", "host" and "host_header".  The optional field is port (defaults to 80).
  
```
  "backends" : [
     {
        "name" : "mybackend1",
        "host" : "origin-www.example.com",
        "host_header" : "www.example.com",
        "port" : 8080
     }
  ]  
```  
  
### MatchType

There is a required field for each rule called "match_type".  The only 2 supported values are ANY or ALL.  ANY means that we OR (||) the conditions together and ALL means we AND (&&) the conditions together before executing rule actions.

### Index
Ordered rules require an index so that we can programatically order the rule conditions in VCL.  If you do not specify an "index" field in the ordered_rules block for each rule , you will get an error.  

### FrontEnd 
There is an included AngularJS app in the frontend/ directory for a simple (very simple) UI.  Its not designed, but it should be relatively functional for a quicker, more visual way to create rules/conditions.

If you would like to run the angular app and the API locally you can reference the following nginx snippet to get it working

```
    server {
       listen 80;

       server_name your.server.name ;

       root /path/to/vclgenie/frontend/app;

       location / {
        index index.html ;
       }

       location /bower_components/ {
          root /path/to/vclgenie/frontend/ ;
        }

       location /api/ {
          proxy_pass http://localhost:9000/ ;
       }

     }
```   