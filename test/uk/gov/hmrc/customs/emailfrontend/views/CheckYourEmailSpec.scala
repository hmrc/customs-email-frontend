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
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.customs.emailfrontend.controllers.routes
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.confirmEmailForm
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.customs.emailfrontend.utils.ViewTestHelper
import uk.gov.hmrc.customs.emailfrontend.views.html.check_your_email

class CheckYourEmailSpec extends ViewTestHelper {

  "CheckYourEmail" should {
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
        shouldContainBackLinkUrl(view, routes.WhatIsYourEmailController.whatIsEmailAddress.url)
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

    val linkUrl = "test.com"

    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequestWithCsrf("GET", "/some/resource/path")

    val view: Document =
      Jsoup.parse(
        app.injector
          .instanceOf[check_your_email]
          .apply(confirmEmailForm, "linkUrl")(request = request, messages = messages)
          .body
      )
  }
}
