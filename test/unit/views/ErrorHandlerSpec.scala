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
import play.api.i18n.MessagesApi
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.views.html.partials.error_template

class ErrorHandlerSpec extends ViewSpec {

  private val view = app.injector.instanceOf[error_template]

  implicit def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  private val errorHandler = new ErrorHandler(messagesApi, view)

  "standardErrorTemplate" should {
    val result = Jsoup.parse(contentAsString(errorHandler.standardErrorTemplate("Some Title", "Some Heading", "Some Message Content")))

    "have the correct title" in {
      result.title mustBe "Some Title"
    }
    "have the correct heading" in {
      result.getElementsByTag("h1").text mustBe "Some Heading"
    }
    "have the correct message" in {
      result.getElementById("main-content").text mustBe "Some Message Content"
    }
  }
}
