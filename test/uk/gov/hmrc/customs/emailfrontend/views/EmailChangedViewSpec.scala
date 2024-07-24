/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, stubMessages}
import play.twirl.api.Html
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.views.html.email_changed

class EmailChangedViewSpec extends SpecBase {

  "EmailChangedView" must {

    "render the email changed page correctly" in new Setup {
      val html: Html = view(newEmail, prevEmail, referrerName, referrerUrl)
      val content: String = contentAsString(html)

      content should include(messages("customs.emailfrontend.email-changed.title-and-heading"))
      content should include(messages("customs.emailfrontend.email-confirmed.info1"))

      content should include(newEmail)
    }
  }

  trait Setup {
    val app: Application = applicationBuilder[FakeIdentifierAgentAction]().build()

    val newEmail: String = "new@example.com"
    val prevEmail: Option[String] = Some("old@example.com")
    val referrerName: Option[String] = Some("referrer")
    val referrerUrl: Option[String] = Some("/referrer")

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")

    implicit val messages: Messages = stubMessages()

    val view: email_changed = app.injector.instanceOf[email_changed]
  }
}
