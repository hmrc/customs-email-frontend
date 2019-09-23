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
import uk.gov.hmrc.customs.emailfrontend.views.html.verify_your_email

class VerifyYourEmailViewSpec extends ViewSpec {
  private val view = app.injector.instanceOf[verify_your_email]

  "What Is Your Email Address page" should {
    "display correct title" in {
      doc.title must startWith("Verify your email address")
    }

    "have the correct h1 text" in {
      doc.body.getElementsByTag("h1").text() mustBe "Verify your email address"
    }

    "have the correct class on the h1" in {
      doc.body.getElementsByTag("h1").hasClass("heading-large") mustBe true
    }

    "have an change your email address 'text' and change email link" in {
      doc.body.getElementById("p2").text() mustBe "You can change your email address if it is not correct."
      doc.body.getElementById("p2").select("a[href]").attr("href") mustBe "/customs-email-frontend/change-email-address"
    }

    "have an link send it again" in {
      doc.body.getElementById("p3").select("a[href]").attr("href") mustBe "/customs-email-frontend/check-email-address"
    }
  }

  lazy val doc: Document = Jsoup.parse(contentAsString(view.render("test@test.com", request, messages)))
}
