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

package uk.gov.hmrc.customs.emailfrontend.views.components

import play.api.Application
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers._
import play.api.test.Helpers
import play.twirl.api.{HtmlFormat, Html}
import play.api.i18n.Messages
import uk.gov.hmrc.customs.emailfrontend.utils.{
  SpecBase,
  FakeIdentifierAgentAction
}
import uk.gov.hmrc.customs.emailfrontend.views.html.components.fieldset

class FieldsetSpec extends SpecBase {

  "FieldSet Component" should {
    "render fieldset with legend as heading" in new Setup {

      running(app) {
        val fieldsetView = app.injector.instanceOf[fieldset]
        val content = Html("<div>Fieldset content</div>")

        val output: HtmlFormat.Appendable = fieldsetView(
          legend = "Heading",
          asHeading = true,
          describedBy = None
        )(content)(messages)

        val html: Document = Jsoup.parse(contentAsString(output))

        html.getElementsByClass("govuk-fieldset__legend")
          .attr("class") should include("govuk-fieldset__legend--xl")
      }
    }

    "render fieldset with legend not as heading" in new Setup {

      running(app) {
        val fieldsetView = app.injector.instanceOf[fieldset]
        val content = Html("<div>Fieldset content</div>")

        val output: HtmlFormat.Appendable = fieldsetView(
          legend = "Heading",
          asHeading = false,
          describedBy = None
        )(content)(messages)

        val html: Document = Jsoup.parse(contentAsString(output))

        html.getElementsByClass("govuk-fieldset__legend")
          .attr("class") should not include ("govuk-fieldset__legend--xl")
      }

    }
  }

  trait Setup {
    implicit val messages: Messages = Helpers.stubMessages()
    val app: Application = applicationBuilder[FakeIdentifierAgentAction]().build()
  }
}
