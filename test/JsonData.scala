import play.api.libs.json.Json

/**
 * Created by 1110109 on 8/18/15.
 */
trait JsonData {

  val ruleJson = Json.parse("""[
          {
            "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                             { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"}],
            "actions" : [ { "action" : "http_redirect", "value" : "http://google.com" } ],
            "match_type" : "ALL"

          }
        ]""")

  val ruleJson2 = Json.parse("""[
           {
            "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                             { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"}],
            "actions" : [ { "action" : "http_redirect", "value" : "http://google.com" } ],
            "match_type" : "ANY"
          }
        ]""")

  val ruleJson3 = Json.parse("""[
           {
            "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                             { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"}],
            "actions" : [ { "action" : "http_redirect", "value" : "http://google.com" } ],
            "match_type" : "BADVAL"
          }
        ]""")

  val ruleJson4 = Json.parse("""[
           {
            "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                             { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"}],
            "actions" : [ { "action" : "http_redirect", "value" : "http://google.com" },
                          { "action" : "http_redirect", "value" : "http://yahoo.com" }
                        ],
            "match_type" : "ALL"
          }
        ]""")

  val ruleJson5 = Json.parse("""[
           {
            "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                             { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"}],
            "actions" : [ { "action" : "add_cookie", "value" : "http://google.com" }
                        ],
            "match_type" : "ALL"
          }
        ]""")

  val ruleJson6 = Json.parse("""[
           {
            "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                             { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"}],
            "actions" : [ { "action" : "remove_cookies" }
                        ],
            "match_type" : "ALL"
          }
        ]""")

  val ruleJson7 = Json.parse("""[
           {
            "conditions" : [ { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue", "name" : "somename" },
                             { "condition" : "cookie", "matcher" : "matches", "value" : "somevalue2", "name" : "somename2"}],
            "actions" : [ { "action" : "remove_request_header" }
                        ],
            "match_type" : "ALL"
          }
        ]""")



}
