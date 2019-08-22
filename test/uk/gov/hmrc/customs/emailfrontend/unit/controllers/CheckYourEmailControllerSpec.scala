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

package uk.gov.hmrc.customs.emailfrontend.unit.controllers

import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.CheckYourEmailController
import uk.gov.hmrc.customs.emailfrontend.services.EmailVerificationService
import uk.gov.hmrc.customs.emailfrontend.views.html.check_your_email

class CheckYourEmailControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[check_your_email]
  private val mockEmailVerificationService = mock[EmailVerificationService]


  private val controller = new CheckYourEmailController(fakeAction, view, mockEmailVerificationService)

  "ConfirmEmailController" should {

    "have a status of OK(200)" in withAuthorisedUser() {
      val eventualResult = controller.show(request)
      status(eventualResult) shouldBe OK
    }

    "have a status of Bad Request (400) when no selection is provided" in withAuthorisedUser() {
      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "")

      val eventualResult = controller.submit(request)
      status(eventualResult) shouldBe BAD_REQUEST
    }

    "have a status of OK (200) when yes is selected" in withAuthorisedUser() {
      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "true")

      val eventualResult = controller.submit(request)
      status(eventualResult) shouldBe OK
    }

    "have a status of OK (200) when no is selected" in withAuthorisedUser() {
      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "false")

      val eventualResult = controller.submit(request)
      status(eventualResult) shouldBe OK
    }
  }
}
