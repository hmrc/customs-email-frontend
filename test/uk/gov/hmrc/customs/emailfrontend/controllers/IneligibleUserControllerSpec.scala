/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.controllers

import play.api.Application
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.model.Ineligible
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.views.html.ineligible_user

class IneligibleUserControllerSpec extends SpecBase {

  "IneligibleUserController" should {
    "have a status of Unauthorised (401)" in {
      val app: Application = applicationBuilder[FakeIdentifierAgentAction]().build()

      val view = app.injector.instanceOf[ineligible_user]
      val controller = app.injector.instanceOf[IneligibleUserController]
      val messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

      running(app) {
        val request = FakeRequest()
        val eventualResult = controller.show(Ineligible.NoEnrolment).apply(request)
        status(eventualResult) shouldBe UNAUTHORIZED
        contentAsString(eventualResult) shouldBe view(Ineligible.NoEnrolment)(request, messages).toString

      }
    }
  }
}
