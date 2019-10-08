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
import org.jsoup.nodes.Document
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.views.html.problem_with_this_service

class ProblemWithThisServiceViewSpec extends ViewSpec {
  private val view = app.injector.instanceOf[problem_with_this_service]
  private val doc: Document = Jsoup.parse(contentAsString(view.render(request, messages)))

  "Problem With This Service page" should {
    "have the correct title" in {
      doc.title mustBe "Sorry, there is a problem with the service"
    }

    "have the correct heading" in {
      doc.getElementsByTag("h1").text mustBe "Sorry, there is a problem with the service"
    }

    "have the correct content" in {
      doc.getElementById("info").text mustBe "Your email has not been updated. Try again later."
    }

    "have the sign out button" in {
      doc.getElementsByClass("button").text mustBe "Sign out"
      doc.getElementsByClass("button").attr("href") mustBe "/manage-email-cds/signout"
    }
  }
}
