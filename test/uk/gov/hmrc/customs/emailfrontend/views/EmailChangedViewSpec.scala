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
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, stubMessages}
import play.api.test.{FakeHeaders, FakeRequest}
import play.twirl.api.Html
import uk.gov.hmrc.customs.emailfrontend.views.html.email_changed
import uk.gov.hmrc.customs.emailfrontend.utils.{SpecBase, FakeIdentifierAgentAction}
import uk.gov.hmrc.customs.emailfrontend.controllers.routes
import java.time.{Instant, OffsetDateTime, ZoneOffset}
import play.api.libs.json.Json

class EmailChangedViewSpec extends SpecBase {

  "EmailChangedView" must {

    "render the email changed page correctly" in new Setup {
      val html: Html = view(newEmail, prevEmail, referrerName, referrerUrl)
      val content: String = contentAsString(html)

      // Verify the title and headings are included
      content should include(messages("customs.emailfrontend.email-changed.title-and-heading"))
      content should include(messages("customs.emailfrontend.email-confirmed.info1"))

      // Verify the new email is displayed
      content should include(newEmail)

    }
  }

  trait Setup {
    val app: Application = applicationBuilder[FakeIdentifierAgentAction]().build()

    val newEmail: String = "new@example.com"
    val prevEmail: Option[String] = Some("old@example.com")
    val referrerName: Option[String] = Some("referrer")
    val referrerUrl: Option[String] = Some("/referrer")
    val eori: String = "EORINOTIMESTAMP"

    val mandatoryHeaders: Seq[(String, String)] = Seq(
      "Date" -> OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC).toString,
      "X-Correlation-ID" -> "8b3c1eb9-7b17-49b8-a32b-b4a1566cb5d4",
      "X-Forwarded-Host" -> "0.0.0.0",
      "Accept" -> "application/json"
    )

    def queryParameters(eori: String): String =
      s"?EORI=$eori&regime=CDS&acknowledgementReference=11a2b17559e64b14be257a112a7d9e8e"

    implicit val request: FakeRequest[_] = FakeRequest(
      "GET",
      routes.EmailConfirmedController.show.url + queryParameters(eori),
      FakeHeaders(mandatoryHeaders),
      Json.parse("{}")
    )

    implicit val messages: Messages = stubMessages()

    val view: email_changed = app.injector.instanceOf[email_changed]
  }
}
