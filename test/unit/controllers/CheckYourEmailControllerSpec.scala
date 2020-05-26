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

package unit.controllers

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers._
import play.twirl.api.Html
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.controllers.CheckYourEmailController
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.customs.emailfrontend.services.{
  EmailVerificationService,
  Save4LaterService
}
import uk.gov.hmrc.customs.emailfrontend.views.html.check_your_email

import scala.concurrent.Future

class CheckYourEmailControllerSpec extends ControllerSpec with ScalaFutures {

  private val view = app.injector.instanceOf[check_your_email]
  private val mockEmailVerificationService = mock[EmailVerificationService]
  private val mockSave4LaterService = mock[Save4LaterService]
  private val mockErrorHandler = mock[ErrorHandler]
  private val controller = new CheckYourEmailController(
    fakeAction,
    view,
    mockEmailVerificationService,
    mcc,
    mockSave4LaterService,
    mockErrorHandler
  )

  "ConfirmEmailController" should {

    "have a status of OK when email found in cache" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(
          Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of SEE_OTHER when email not found in cache on show" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/signout")
    }

    "have a status of SEE_OTHER when email not found in cache on submit" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(Future.successful(None))

      val request: Request[AnyContentAsFormUrlEncoded] =
        requestWithForm("isYes" -> "")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/signout")
    }

    "have a status of BAD_REQUEST when no selection is provided" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(
          Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

      val request: Request[AnyContentAsFormUrlEncoded] =
        requestWithForm("isYes" -> "")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe BAD_REQUEST
    }

    "have a status of SEE_OTHER when no is selected" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(
          Future.successful(Some(EmailDetails(None, "abc@def.com", None))))
      when(mockSave4LaterService.remove(any())(any(), any()))
        .thenReturn(Future.successful(()))

      val request: Request[AnyContentAsFormUrlEncoded] =
        requestWithForm("isYes" -> "false")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/change-email-address/create")
    }

    "have a status of SEE_OTHER when user clicks back on the successful request or uses already complete bookmarked request within 2 hours" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(Future.successful(
          Some(EmailDetails(None, "abc@def.com", Some(DateTime.now())))))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/cannot-change-email")
    }

    "redirect to 'there is a problem with the service' page" in withAuthorisedUser() {
      when(mockErrorHandler.problemWithService()(any()))
        .thenReturn(Html("Sorry, there is a problem with the service"))

      val request: Request[AnyContentAsFormUrlEncoded] =
        requestWithForm("email" -> "")
      val eventualResult = controller.problemWithService()(request)

      status(eventualResult) shouldBe BAD_REQUEST
      contentAsString(eventualResult).contains(
        "Sorry, there is a problem with the service") shouldBe true
    }
  }

  "ConfirmEmailController on submit with yes selected" should {

    "redirect to Verify Your Email  page when email yet not verified" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(
          Future.successful(Some(EmailDetails(None, "abc@def.com", None))))
      when(mockSave4LaterService.saveEmail(any(), any())(any()))
        .thenReturn(Future.successful(()))
      when(
        mockEmailVerificationService
          .createEmailVerificationRequest(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(false)))

      val request: Request[AnyContentAsFormUrlEncoded] =
        requestWithForm("isYes" -> "true")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/email-address-confirmed")
    }

    "redirect to Email Confirmed page when email already verified" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(
          Future.successful(Some(EmailDetails(None, "abc@def.com", None))))
      when(
        mockEmailVerificationService
          .createEmailVerificationRequest(any(), any(), any())(any()))
        .thenReturn(Future.successful(Some(true)))

      val request: Request[AnyContentAsFormUrlEncoded] =
        requestWithForm("isYes" -> "true")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/manage-email-cds/confirm-email-address")
    }

    "show 'there is a problem with service' page when createEmailVerificationRequest failed" in withAuthorisedUser() {
      when(mockSave4LaterService.fetchEmail(any())(any(), any()))
        .thenReturn(
          Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

      when(
        mockEmailVerificationService
          .createEmailVerificationRequest(any(), any(), any())(any()))
        .thenReturn(Future.successful(None))

      when(mockErrorHandler.problemWithService()(any()))
        .thenReturn(Html("Sorry, there is a problem with the service"))

      val request: Request[AnyContentAsFormUrlEncoded] =
        requestWithForm("isYes" -> "true")

      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith(
        "/problem-with-this-service")
    }
  }
}
