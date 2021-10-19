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

package unit.controllers

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito.{times, verify, _}
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers.{status, _}
import play.twirl.api.Html
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.controllers.EmailConfirmedController
import uk.gov.hmrc.customs.emailfrontend.model.{
  EmailDetails,
  InternalId,
  ReferrerName
}
import uk.gov.hmrc.customs.emailfrontend.services.{
  CustomsDataStoreService,
  DateTimeService,
  EmailVerificationService,
  Save4LaterService,
  UpdateVerifiedEmailService
}
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException}

import scala.concurrent.{ExecutionContext, Future}

class EmailConfirmedControllerSpec
  extends ControllerSpec
    with BeforeAndAfterEach {

  private val view = app.injector.instanceOf[email_confirmed]
  private val mockSave4LaterService = mock[Save4LaterService]
  private val mockCustomsDataStoreService = mock[CustomsDataStoreService]
  private val mockEmailVerificationService = mock[EmailVerificationService]
  private val mockUpdateVerifiedEmailService = mock[UpdateVerifiedEmailService]
  private val mockErrorHandler = mock[ErrorHandler]
  private val mockDateTimeService = mock[DateTimeService]
  private val testDateTime = DateTime.parse("2021-01-01T11:11:11.111Z")
  private val controller = new EmailConfirmedController(
    fakeAction,
    view,
    mockCustomsDataStoreService,
    mockSave4LaterService,
    mockEmailVerificationService,
    mockUpdateVerifiedEmailService,
    mcc,
    mockErrorHandler,
    mockDateTimeService
  )

  override protected def beforeEach(): Unit = {
    reset(
      mockCustomsDataStoreService,
      mockEmailVerificationService,
      mockSave4LaterService,
      mockUpdateVerifiedEmailService,
      mockDateTimeService
    )
    when(mockDateTimeService.nowUtc()).thenReturn(testDateTime)
  }

  "EmailConfirmedController" should {
    "return OK " when {
      "email found in cache, email is verified and update verified email is successful" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(meq(None),
          meq("abc@def.com"),
          meq("GB1234567890"),
          meq(testDateTime))(any[HeaderCarrier])
        ).thenReturn(Future.successful(Some(true)))

        when(mockSave4LaterService.remove(meq(InternalId("internalId")))(any(), any())).thenReturn(Future.successful(()))

        when(mockSave4LaterService.saveEmail(meq(InternalId("internalId")), any[EmailDetails])(any()))
          .thenReturn(Future.successful(()))

        when(mockSave4LaterService.fetchReferrer(meq(InternalId("internalId")))(any(), any()))
          .thenReturn(Future.successful(Some(ReferrerName("abc", "/xyz"))))

        when(mockCustomsDataStoreService.storeEmail(meq(EnrolmentIdentifier("EORINumber", "GB1234567890")), meq("abc@def.com"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe OK
      }

    }

    "return REDIRECT to confirm email page" when {
      "email found in cache but email is not verified" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockCustomsDataStoreService.storeEmail(meq(EnrolmentIdentifier("EORINumber", "GB1234567890")), meq("abc@def.com"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe SEE_OTHER

        redirectLocation(eventualResult).value should endWith("/manage-email-cds/confirm-email-address")
      }

      "when email found in cache but isEmailVerified failed" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))
        when(mockCustomsDataStoreService.storeEmail(meq(EnrolmentIdentifier("EORINumber", "GB1234567890")), meq("abc@def.com"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse(OK, "")))
        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any[HeaderCarrier])).thenReturn(Future.successful(None))

        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe SEE_OTHER

        redirectLocation(eventualResult).value should endWith("/manage-email-cds/confirm-email-address")
      }

    }

    "return REDIRECT to sign-out page" when {
      "email not found in cache" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(None))

        val eventualResult = controller.show(request)

        status(eventualResult) shouldBe SEE_OTHER
        redirectLocation(eventualResult).value should endWith("/manage-email-cds/signout")
      }

    }

    "return REDIRECT to cannot change email page" when {
      "user retries the same request(user click back on successful request or refreshes the browser)" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", Some(DateTime.now())))))

        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe SEE_OTHER
        redirectLocation(eventualResult).value should endWith("/manage-email-cds/cannot-change-email")
      }
    }

    "return REDIRECT to problem page" when {
      "email found in cache, email is verified and update verified email is successful but saving timestamp fails" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(meq(None), meq("abc@def.com"), meq("GB1234567890"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockSave4LaterService.saveEmail(meq(InternalId("internalId")), any())(any()))
          .thenReturn(Future.failed(new InternalServerException("")))

        when(mockSave4LaterService.fetchReferrer(any())(any(), any()))
          .thenReturn(Future.successful(Some(ReferrerName("abc", "/xyz"))))

        when(mockCustomsDataStoreService.storeEmail(meq(EnrolmentIdentifier("EORINumber", "GB1234567890")), meq("abc@def.com"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        when(mockErrorHandler.problemWithService()(any()))
          .thenReturn(Html("Sorry, there is a problem with the service"))

        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe SEE_OTHER
        redirectLocation(eventualResult).value should endWith("/problem-with-this-service")
      }

      "save email is failed with Error 400 or 500" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(meq(None), meq("abc@def.com"), meq("GB1234567890"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(None))

        when(mockErrorHandler.problemWithService()(any()))
          .thenReturn(Html("Sorry, there is a problem with the service"))

        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe SEE_OTHER
        redirectLocation(eventualResult).value should endWith("/problem-with-this-service")
      }

      "save email returns 200 with no form bundle id param" in withAuthorisedUser() {
        when(mockSave4LaterService.fetchEmail(any())(any(), any()))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(meq(None), meq("abc@def.com"), meq("GB1234567890"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(false)))

        when(mockErrorHandler.problemWithService()(any()))
          .thenReturn(Html("Sorry, there is a problem with the service"))

        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe SEE_OTHER
        redirectLocation(eventualResult).value should endWith(
          "/problem-with-this-service")
      }
    }

    "return REDIRECT to ineligible" when {
      "user has no enrolments" in withAuthorisedUserWithoutEnrolments {
        val eventualResult = controller.show(request)
        status(eventualResult) shouldBe SEE_OTHER

        redirectLocation(eventualResult).value should endWith(
          "/manage-email-cds/ineligible/no-enrolment")

        verify(mockCustomsDataStoreService, times(0))
          .storeEmail(any(), any(), any())(any[HeaderCarrier])
        verify(mockEmailVerificationService, times(0))
          .isEmailVerified(any())(any[HeaderCarrier])
        verify(mockSave4LaterService, times(0))
          .fetchEmail(any())(any[HeaderCarrier], any[ExecutionContext])
        verify(mockUpdateVerifiedEmailService, times(0))
          .updateVerifiedEmail(any(), any(), any(), any())(any[HeaderCarrier])
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
  }

}
