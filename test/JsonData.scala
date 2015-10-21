import play.api.libs.json.Json

trait JsonData {

  val ruleJson = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"}
                                                         ],
                                         "actions" : [ { "action" : "set_ttl", "value" : "600", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ALL",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "set_ttl", "value" : "2", "units" : "HOURS" } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }],

                                 "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")

  val ruleJson2 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"},
                                                           { "condition" : "client_ip" , "matcher" : "matches", "value" : "3.3.3.3" }
                                                         ],
                                         "actions" : [ { "action" : "http_redirect", "value" : "http://www.google.com" }
                                                      ],
                                          "match_type" : "ALL",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "set_ttl", "value" : "2", "units" : "HOURS" } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")


  val ruleJson3 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"},
                                                           { "condition" : "client_ip" , "matcher" : "matches", "value" : "3.3.3.3" }
                                                         ],
                                         "actions" : [ { "action" : "http_redirect", "value" : "http://www.google.com" }
                                                      ],
                                          "match_type" : "BADVAL",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "set_ttl", "value" : "2", "units" : "HOURS" } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }],

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")

  val ruleJson4 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"},
                                                           { "condition" : "client_ip" , "matcher" : "matches", "value" : "3.3.3.3" }
                                                         ],
                                         "actions" : [
                                           { "action" : "http_redirect", "value" : "http://www.google.com" },
                                           { "action" : "http_redirect", "value" : "http://www.google.com" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "set_ttl", "value" : "2", "units" : "HOURS" } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")


  val ruleJson5 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"},
                                                           { "condition" : "client_ip" , "matcher" : "matches", "value" : "3.3.3.3" }
                                                         ],
                                         "actions" : [
                                           { "action" : "add_cookie", "value" : "http://www.google.com" }
                                                      ],
                                          "match_type" : "BADVAL",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "set_ttl", "value" : "2", "units" : "HOURS" } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")



  val ruleJson6 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"},
                                                           { "condition" : "client_ip" , "matcher" : "matches", "value" : "3.3.3.3" }
                                                         ],
                                         "actions" : [
                                           { "action" : "remove_cookies", "value" : "somecookie" }
                                                      ],
                                          "match_type" : "ALL",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "set_ttl", "value" : "2", "units" : "HOURS" } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")


  val ruleJson7 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"},
                                                           { "condition" : "client_ip" , "matcher" : "matches", "value" : "3.3.3.3" }
                                                         ],
                                         "actions" : [
                                           { "action" : "remove_cookies", "value" : "somecookie" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "remove_request_header"  } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")


  val ruleJson8 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                                                           { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"},
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/cookieurl"},
                                                           { "condition" : "client_ip" , "matcher" : "matches", "value" : "3.3.3.3" }
                                                         ],
                                         "actions" : [
                                           { "action" : "remove_cookies", "value" : "somecookie" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" },
                                                        { "action" : "set_backend", "value" : "backend2" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "remove_request_header", "value" : "X-HTTP-Header"  } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")


 val  ruleJson9 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [
                                                           { "condition" : "is_cached"  }
                                                         ],
                                         "actions" : [
                                           { "action" : "set_ttl", "value" : "300" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 2
                                        },

                                      {
                                          "conditions" : [ { "condition" : "request_url", "matcher" : "matches", "value" : "/home" },
                                                           { "condition" : "request_header", "matcher" : "matches", "value" : "someheadervalue", "name" : "X-HTTP-Something"}],
                                          "actions" : [ { "action" : "set_ttl", "value" : "300", "units" : "SECONDS" },
                                                        { "action" : "set_backend", "value" : "backend1" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [
                                       {
                                          "conditions" : [ { "condition" : "request_header", "matcher" : "equals", "value" : "globalvalue", "name" : "X-HTTP-Test" },
                                                           { "condition" : "request_url", "matcher" : "matches", "value" : "/redirecter"}
                                                         ],
                                          "actions" : [ { "action" : "remove_request_header", "value" : "X-HTTP-Header"  } ],
                                          "match_type" : "ALL"

                                        }
                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")


  val  ruleJson10 = Json.parse("""{
                                  "ordered_rules" :
                                      [
                                         {
                                          "conditions" : [
                                                           { "condition" : "is_cached"  }
                                                         ],
                                         "actions" : [
                                           { "action" : "add_cookie", "name" : "cachecookie", "value" : "somecookievalue" }
                                                      ],
                                          "match_type" : "ANY",
                                          "index" : 1
                                        }
                                      ],

                                  "global_rules" : [

                                      ],

                                "hostnames" :
                                     [ { "hostname" : "www.yahoo.com" }, { "hostname" : "www.microsoft.com" }] ,

                                "backends" : [ { "name" : "backend1" , "host" : "www.myhost.com", "host_header" : "www.myhost.com"} ]

                              }""")



}
