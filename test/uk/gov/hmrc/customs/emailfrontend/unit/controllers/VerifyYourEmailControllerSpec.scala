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

import uk.gov.hmrc.customs.emailfrontend.controllers.VerifyYourEmailController
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.customs.emailfrontend.services.EmailCacheService
import uk.gov.hmrc.customs.emailfrontend.views.html.verify_your_email
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.test.Helpers._


import scala.concurrent.Future

class VerifyYourEmailControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[verify_your_email]
  private val mockEmailCacheService = mock[EmailCacheService]

  private val controller = new VerifyYourEmailController(fakeAction, view, mockEmailCacheService, mcc)

  "VerifyYourEmailController" should {
    "redirect to sign out page when no email found in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/signout")
    }

    "return status OK when email found in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("testEmail"))))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe OK
    }
  }
}
