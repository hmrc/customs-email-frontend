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
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed

class EmailConfirmedViewSpec extends ViewSpec {
  private val view = app.injector.instanceOf[email_confirmed]
  private val oldEmail: Option[String] = Some("oldEmail@email.com")
  private val newEmail: String = "newEmail@email.com"
  private val doc: Document = Jsoup.parse(contentAsString(view.render(newEmail, oldEmail, request, messages)))

  "Confirm Email page" should {
    "have the correct title" in {
      doc.title mustBe "Email address confirmed"
    }

    "have the correct heading" in {
      doc.getElementsByTag("h1").text mustBe "Email address confirmed"
    }

    "have the correct content" in {
      doc.getElementById("info1").text mustBe s"Your email address $newEmail will be active in 2 hours."
      doc.getElementById("info2").text mustBe s"Until then we will send CDS emails to ${oldEmail.get}."
    }

    "have the sign out button" in {
      doc.getElementsByClass("button").text mustBe "Sign out"
      doc.getElementsByClass("button").attr("href") mustBe "/manage-email-cds/signout"
    }
  }
}
