/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.views.html.change_your_email
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm

class ChangeYourEmailViewSpec extends SpecBase {

  "ChangeYourEmail" should {
    "display correct guidance" when {

      "banner is visible" in new Setup {
        shouldContainCorrectBanners(view)
      }

      "title is visible" in new Setup {
        shouldContainCorrectTitle(view)
      }

      "service urls are visible" in new Setup {
        shouldContainCorrectServiceUrls(view.html())
      }

      "backlink is visible" in new Setup {
        shouldContainCorrectBackLink(view, Option(linkUrl))
      }
    }
  }

  private def shouldContainCorrectTitle(viewDoc: Document, title: String = emptyString) =
    if (title.nonEmpty) {
      viewDoc.title() mustBe title
    } else {
      viewDoc.title() mustBe "GOV.UK - The best place to find government services and information"
    }

  private def shouldContainCorrectServiceUrls(viewDoc: String) = {
    viewDoc.contains(uk.gov.hmrc.customs.emailfrontend.controllers.routes.SignOutController.signOut.url) mustBe true
    viewDoc.contains("/accessibility-statement/manage-email-cds") mustBe true
  }

  private def shouldContainCorrectBackLink(viewDoc: Document, backLinkUrl: Option[String] = None) =
    if (backLinkUrl.isDefined) {
      viewDoc.getElementsByClass("govuk-back-link").text() mustBe "Back"
      viewDoc
        .getElementsByClass("govuk-back-link")
        .attr("href")
        .contains(backLinkUrl.get) mustBe true
    } else {
      viewDoc.getElementsByClass("govuk-back-link").text() mustBe "Back"
      viewDoc
        .getElementsByClass("govuk-back-link")
        .attr("href")
        .contains("#") mustBe true
    }

  private def shouldContainCorrectBanners(viewDoc: Document) =
    viewDoc
      .getElementsByClass("govuk-phase-banner")
      .text() mustBe "BETA This is a new service – your feedback will help us to improve it."

  trait Setup {
    val app: Application = applicationBuilder[FakeIdentifierAgentAction]()
      .configure("play.filters.csrf.enabled" -> "false")
      .build()

    val linkUrl = "test.com"

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(
      "GET", "/some/resource/path")

    implicit val msgs: Messages = app.injector.instanceOf[MessagesApi].preferred(
      fakeRequest(emptyString, emptyString))

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

    val view: Document = Jsoup.parse(app.injector.instanceOf[change_your_email].apply(
      emailForm, appConfig)(request = request, messages = msgs).body)
  }
}
