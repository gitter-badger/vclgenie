import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.Logger
import play.api.libs.json.Json

import play.api.test._
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class VclControllerSpec extends Specification with JsonData {

  "Application" should {


    "return 200 with a valid VCL" in new WithApplication {
      val Some(result) = route(FakeRequest(POST, "/vcl").withJsonBody(ruleJson))
      status(result) must equalTo(OK)
      val bodyText: String = contentAsString(result)
      bodyText must contain("Global Rules")
    }

    "return 200 with a valid VCL 2" in new WithApplication {
      val Some(result) = route(FakeRequest(POST, "/vcl").withJsonBody(ruleJson2))
      status(result) must equalTo(OK)
      val bodyText: String = contentAsString(result)
      bodyText must contain("Global Rules")
    }

    "not accept an invalid matchType" in new WithApplication {
      val Some(result) = route(FakeRequest(POST, "/vcl").withJsonBody(ruleJson3))
      status(result) must equalTo(BAD_REQUEST)
    }

    "accept only one SingleAction action" in new WithApplication {
      val Some(result) = route(FakeRequest(POST, "/vcl").withJsonBody(ruleJson4))
      status(result) must equalTo(BAD_REQUEST)
      val bodyText: String = contentAsString(result)
      bodyText must contain("Only a single action of type SingleAction is permitted")
    }

    "not allow a nameVal action without name and value present" in new WithApplication {
      val Some(result) = route(FakeRequest(POST,"/vcl").withJsonBody(ruleJson5))
      status(result) must equalTo(BAD_REQUEST)
      val bodyText: String = contentAsString(result)
      bodyText must contain("NameVal actions must have name and value")
    }

    "not allow a boolean action to have a missing name or value other than 0 or 1" in new WithApplication {
      val Some(result) = route(FakeRequest(POST,"/vcl").withJsonBody(ruleJson6))
      status(result) must equalTo(BAD_REQUEST)
      val bodyText = contentAsString(result)
      bodyText must contain("Boolean action type requires value")
    }

    "not allow a name action to have a missing name" in new WithApplication {
      val Some(result) = route(FakeRequest(POST,"/vcl").withJsonBody(ruleJson7))
      status(result) must equalTo(BAD_REQUEST)
      val bodyText = contentAsString(result)
      bodyText must contain("actions of type NameAction must have a name")
    }


  }
}
