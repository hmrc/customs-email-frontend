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

package unit.controllers

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.CheckYourEmailController
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html.check_your_email
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class CheckYourEmailControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[check_your_email]
  private val mockEmailVerificationService = mock[EmailVerificationService]
  private val mockEmailCacheService = mock[EmailCacheService]

  private val controller = new CheckYourEmailController(fakeAction, view, mockEmailVerificationService, mcc, mockEmailCacheService)

  "ConfirmEmailController" should {

    "have a status of OK when email found in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@test.com"))))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of SEE_OTHER when email not found in cache on show" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/signout")
    }

    "have a status of SEE_OTHER when email not found in cache on submit" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/signout")
    }

    "have a status of BAD_REQUEST when no selection is provided" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@test.com"))))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe BAD_REQUEST
    }

    "have a status of SEE_OTHER when no is selected" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@test.com"))))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "false")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/email-address/create")
    }
  }

  "ConfirmEmailController on submit with yes selected" should {

    "redirect to Verify Your Email  page when email yet not verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@test.com"))))
      when(mockEmailCacheService.saveEmail(any(), any())(any(), any())).thenReturn(Future.successful(CacheMap("testId", Map())))
      when(mockEmailVerificationService.createEmailVerificationRequest(any(), any())(any())).thenReturn(Future.successful(Some(false)))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "true")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/email-address-confirmed")
    }

    "redirect to Email Confirmed page when email already verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@test.com"))))
      when(mockEmailVerificationService.createEmailVerificationRequest(any(), any())(any())).thenReturn(Future.successful(Some(true)))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "true")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/confirm-email-address")
    }

    "throw exception when createEmailVerificationRequest failed" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@test.com"))))
      when(mockEmailVerificationService.createEmailVerificationRequest(any(), any())(any())).thenReturn(Future.successful(None))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("isYes" -> "true")

      val result = intercept[IllegalStateException] {
        await(controller.submit(request))
      }

      result.getMessage shouldBe "CreateEmailVerificationRequest Failed"
    }
  }
}
