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
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.controllers.routes
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.customs.emailfrontend.utils.ViewTestHelper
import uk.gov.hmrc.customs.emailfrontend.views.html.change_your_email

class ChangeYourEmailSpec extends ViewTestHelper {

  "ChangeYourEmail" should {
    "display correct guidance" when {

      "banner is visible" in new Setup {
        shouldContainCorrectBanners(view)
      }

      "title is visible" in new Setup {
        shouldContainCorrectTitle(view)
      }

      "service urls are visible" in new Setup {
        shouldContainCorrectServiceUrls(view.html(), routes.SignOutController.signOut.url)
      }

      "backlink is visible" in new Setup {
        shouldContainBackLinkUrl(view, routes.VerifyChangeEmailController.create.url)
      }
    }
  }

  private def shouldContainCorrectTitle(viewDoc: Document, title: String = emptyString)(implicit messages: Messages) = {
    val expectedTitle = if (title.nonEmpty) {
      s"$title - ${messages("service.name")}"
    } else {
      messages("service.name")
    }
    viewDoc.title() should include(expectedTitle)
  }

  trait Setup {
    val app: Application = applicationBuilder()
      .configure("play.filters.csrf.enabled" -> "false")
      .build()

    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequestWithCsrf("GET", "/some/resource/path")

    implicit val msgs: Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

    val view: Document =
      Jsoup.parse(
        app.injector
          .instanceOf[change_your_email]
          .apply(emailForm, appConfig)(request = request, messages = msgs)
          .body
      )
  }
}
