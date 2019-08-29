/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.views

import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.views.html.partials.govuk_wrapper

class GovUkWrapperViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[govuk_wrapper]

  "GovUK Wrapper" should {
    "have a Sign Out link when signed in" in {

      val request2 = FakeRequest("GET", "/").withSession("userId" -> "USERID")
      val doc = Jsoup.parse(contentAsString(view("title")(request2, messages)))
      doc.body().getElementById("sign-out").text mustBe "Sign out"
    }
    "not have a Sign Out link when not signed in" in {
      val doc = Jsoup.parse(contentAsString(view("title")))
      doc.body.getElementById("sign-out") mustBe null
    }
  }
}
