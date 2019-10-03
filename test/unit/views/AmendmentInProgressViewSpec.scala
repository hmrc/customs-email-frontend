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
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.views.html.amendment_in_progress

class AmendmentInProgressViewSpec extends ViewSpec {

  private val view = app.injector.instanceOf[amendment_in_progress]

  private val doc = Jsoup.parse(contentAsString(view.render("test@email.com", request, messages)))

  "Amendment In Progress page" should {
    "have a correct title" in {
      doc.title mustBe "You cannot change your email address"
    }

    "have the correct heading" in {
      doc.getElementsByTag("h1").text mustBe "You cannot change your email address"
    }

    "have the correct content" in {
      doc.getElementById("info").text mustBe "You recently changed your email address to test@email.com. If you need to change this again, you will need to try again in 24 hours."
    }

    "have the sign out button" in {
      doc.getElementsByClass("button").text mustBe "Sign out"
      doc.getElementsByClass("button").attr("href") mustBe "/manage-email-cds/signout"
    }
  }
}
