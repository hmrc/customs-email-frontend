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

package uk.gov.hmrc.customs.emailfrontend.config

import org.jsoup.Jsoup
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.views.html.partials.error_template
import uk.gov.hmrc.customs.emailfrontend.views.html.problem_with_this_service

import scala.concurrent.ExecutionContext
import play.twirl.api.Html

class ErrorHandlerSpec extends SpecBase {

  private val app                      = applicationBuilder[FakeIdentifierAgentAction]().build()
  private val view                     = app.injector.instanceOf[error_template]
  private val customView               = app.injector.instanceOf[problem_with_this_service]
  private val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val ec: ExecutionContext    = ExecutionContext.global
  private val errorHandler             = new ErrorHandler(messagesApi, view, customView)(ec)
  private val request                  = FakeRequest()

  "ErrorHandlerSpec" should {

    "define standardErrorTemplate" in {

      val result = errorHandler.standardErrorTemplate("title", "heading", "message")(request)

      result.map { htmlVal =>
        val doc = Jsoup.parse(contentAsString(htmlVal))
        doc.title                            shouldBe "title"
        doc.body.getElementsByTag("h1").text shouldBe "heading"
        doc.body
          .getElementById("main-content")
          .text                              shouldBe "heading message Is this page not working properly? (opens in new tab)"
      }
    }

    "have custom error view to show 'problem with the service' page" in {

      val result: Html = errorHandler.problemWithService()(request)
      val doc          = Jsoup.parse(contentAsString(result))

      doc.title shouldBe "Sorry, there is a problem with the service - Customs Declaration Service"
    }

    "have correct text error to show 'email has not been updated' page" in {

      val result = errorHandler.problemWithService()(request)
      val doc    = Jsoup.parse(contentAsString(result))

      doc.body
        .getElementsByClass("govuk-body")
        .first()
        .text shouldBe "Your email has not been updated. Try again later."
    }
  }
}
