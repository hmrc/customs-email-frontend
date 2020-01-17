/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.customs.emailfrontend.views.html.partials.error_template

class ErrorTemplateSpec extends ViewSpec {

  private val view = app.injector.instanceOf[error_template]

  private val doc: Document = Jsoup.parse(contentAsString(
    view.render("Some Title", "Some Heading", "Some Message Content", request, messages)))

  "standardErrorTemplate" should {
    "have the correct title" in {
      doc.title mustBe "Some Title"
    }
    "have the correct heading" in {
      doc.body.getElementsByTag("h1").text mustBe "Some Heading"
    }
    "have the correct message" in {
      doc.body.getElementById("main-content").text mustBe "Some Message Content"
    }
  }
}
