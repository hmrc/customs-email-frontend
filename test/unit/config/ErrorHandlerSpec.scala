/*
 * Copyright 2021 HM Revenue & Customs
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

package unit.config

import org.jsoup.Jsoup
import org.scalatest.concurrent.ScalaFutures
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.views.html.partials.error_template
import uk.gov.hmrc.customs.emailfrontend.views.html.problem_with_this_service
import unit.controllers.ControllerSpec

class ErrorHandlerSpec extends ControllerSpec with ScalaFutures {

  private val view = app.injector.instanceOf[error_template]
  private val customView = app.injector.instanceOf[problem_with_this_service]

  private val errorHandler = new ErrorHandler(messagesApi, view, customView)

  "ErrorHandlerSpec" should {
    "define standardErrorTemplate" in {
      val result = errorHandler.standardErrorTemplate("title",
                                                      "heading",
                                                      "message")(request)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe "title"
      doc.body.getElementsByTag("h1").text shouldBe "heading"
      doc.body.getElementById("main-content").text shouldBe "message"
    }

    "have custom error view to show 'problem with the service' page" in {
      val result = errorHandler.problemWithService()(request)
      val doc = Jsoup.parse(contentAsString(result))
      doc.title shouldBe "Sorry, there is a problem with the service"
    }
  }
}
