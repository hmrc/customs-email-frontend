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

package uk.gov.hmrc.customs.emailfrontend.controllers

import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{eq => meq}
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, *}
import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.{
  EmailAlreadyVerified, EmailVerificationRequestFailure, EmailVerificationRequestSent
}
import uk.gov.hmrc.customs.emailfrontend.connectors.{EmailVerificationConnector, SubscriptionDisplayConnector}
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.confirmVerifyChangeForm
import uk.gov.hmrc.customs.emailfrontend.model.*
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.views.html.verify_change_email
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import org.mockito.Mockito.{verify, when, times}
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.BadRequest

import java.time.{LocalDateTime, Period}
import scala.concurrent.Future

class VerifyChangeEmailControllerSpec extends SpecBase
  with BeforeAndAfterEach {

  "VerifyChangeEmailController" should {

    "have a status of SEE_OTHER for show method when email found in cache and " +
      "email status is AmendmentCompleted" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(
          Some(EmailDetails(None, "test@email.com", Some(LocalDateTime.now().minus(Period.ofDays(2)))))))

      when(mockSave4LaterService.remove(any)(any)).thenReturn(Future.successful(Right(())))

      running(app) {

        val request = fakeRequest(GET, routes.VerifyChangeEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.VerifyChangeEmailController.create.url
      }
    }

    "have a status of SEE_OTHER for show method when email is not found in cache and " +
      "email status is AmendmentNotDetermined" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      running(app) {

        val request = fakeRequest(GET, routes.VerifyChangeEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.VerifyChangeEmailController.create.url
      }

    }

    "have a status of SEE_OTHER for show method when email found in cache with no timestamp and " +
      "email is verified" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(None, "test@email.com", None))))

      when(mockEmailVerificationService.isEmailVerified(meq("test@email.com"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(true)))

      running(app) {

        val request = fakeRequest(GET, routes.VerifyChangeEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.EmailConfirmedController.show.url
      }
    }

    "have a status of SEE_OTHER for show method when email found in cache with no timestamp and " +
      "email is not verified" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(None, "test@email.com", None))))

      when(mockEmailVerificationService.isEmailVerified(meq("test@email.com"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(false)))

      running(app) {

        val request = fakeRequest(GET, routes.VerifyChangeEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.CheckYourEmailController.show.url
      }
    }

    "have a status of SEE_OTHER for show method when email found in cache with timestamp " +
      "for AmendmentInProgress" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(None, "test@email.com", Some(LocalDateTime.now())))))

      running(app) {

        val request = fakeRequest(GET, routes.VerifyChangeEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.AmendmentInProgressController.show.url
      }
    }

    "have a status of SEE_OTHER for show method email is not found in cache " in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      running(app) {

        val request = fakeRequest(GET, routes.VerifyChangeEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.VerifyChangeEmailController.create.url
      }
    }

    "have a status of OK for create method when verified email found in subscription display response" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val requestWithForm = fakeRequestWithCsrf(POST, routes.WhatIsYourEmailController.submit.url)
          .withFormUrlEncodedBody(("email", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "have a status of OK for create method when unverified email found in subscription display response" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponseWithNoEmailVerificationTimeStamp))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe OK
      }
    }

    "have a status of SEE_OTHER for create method when no email found in subscription display " +
      "response but returned OK" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(noneSubscriptionDisplayResponse))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of OK for create method when email found in cache with no timestamp" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(Some("old@email"), "test@email.com", None))))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe OK
      }
    }

    "have a status of SEE_OTHER for create method when current email not found in " +
      "cache with no timestamp" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(None, "test@email.com", None))))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of SEE_OTHER for create method when the bookmark url is used and " +
      "user already completed success amend email journey" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(None, "test@email.com", Some(LocalDateTime.now())))))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of SEE_OTHER for create method when unverified email found in subscription " +
      "display response" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponseWithNoEmailVerificationTimeStamp))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe OK
      }
    }

    "show 'there is a problem with the service' page when subscription display is failed" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new HttpException("Failed", BAD_REQUEST)))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "show 'there is a problem with the service' page when subscription display response has " +
      "paramValue 'FAIL' with no email" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponseWithStatus))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "show 'what is your email address' page when subscription display response has no email and " +
      "timestamp with status text and param" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(noneSubscriptionDisplayResponseWithStatus))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.create.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of SEE_OTHER for verify method" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any)).thenReturn(Future.successful(None))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.verifyChangeEmail.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of OK for verify method when email found in cache with no timestamp" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(None))

      running(app) {
        val request = fakeRequestWithCsrf(GET, routes.VerifyChangeEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of SEE_OTHER for verify method when the bookmark url is used " +
      "and user already complete success amend email journey " in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(EmailDetails(None, "test@email.com", Some(LocalDateTime.now())))))

      running(app) {
        val request = fakeRequest(GET, routes.VerifyChangeEmailController.verifyChangeEmail.url)
        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of Bad Request when no email is provided in the form" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val requestWithForm = fakeRequestWithCsrf(POST, routes.VerifyChangeEmailController.verifyChangeEmail.url)
          .withFormUrlEncodedBody(("email", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "have Error: prefixed in the title when confirmVerifyChangeForm has any error and form is submitted" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val requestWithForm: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequest(POST,
            routes.VerifyChangeEmailController.verifyChangeEmail.url).withFormUrlEncodedBody(("isVerify", "None"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe BAD_REQUEST

        contentAsString(result) shouldBe
          view(confirmVerifyChangeForm.bind(Map("isVerify" -> "None")), Some("test@email.com"))(
            requestWithForm, messages(app)).toString()

        val doc = Jsoup.parse(contentAsString(result))
        doc.title should not be empty
        doc.title shouldBe
          s"${messages(app)("site.errorPrefix")} ${
            messages(app)(
              "customs.emailfrontend.verify-change-email.title-and-heading")
          }"
      }
    }

    "show 'there is a problem with the service' page when " +
      "subscription display return response with no email or status for submit" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val request = fakeRequestWithCsrf(POST, routes.VerifyChangeEmailController.verifyChangeEmail.url)
        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "have a status of Bad Request when the email is invalid" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val request = fakeRequestWithCsrf(POST, routes.VerifyChangeEmailController.verifyChangeEmail.url)
          .withFormUrlEncodedBody("email" -> "invalidEmail")

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "have a status of SEE_OTHER when the email is valid" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      running(app) {
        val request = fakeRequestWithCsrf(POST, routes.VerifyChangeEmailController.verifyChangeEmail.url)
          .withFormUrlEncodedBody("email" -> "valid@email.com")

        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
      }
    }

    "show 'there is a problem with the service' page when subscription display is failed for submit" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.failed(new HttpException("Failed", BAD_REQUEST)))

      running(app) {
        val request = fakeRequest(POST, routes.WhatIsYourEmailController.submit.url)
          .withFormUrlEncodedBody("email" -> emptyString)

        val result = route(app, request).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe routes.WhatIsYourEmailController.problemWithService().url
      }
    }

    "redirect to What_Is_Your_Email page when user selects to change the email" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      when(mockSave4LaterService.saveJourneyType(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))
      when(mockSave4LaterService.saveEmail(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))

      running(app) {
        val requestWithForm: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequest(POST, routes.VerifyChangeEmailController.verifyChangeEmail.url)
            .withFormUrlEncodedBody(("isVerify", "false"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER

        redirectLocation(result) shouldBe Some(routes.WhatIsYourEmailController.whatIsEmailAddress.url)
      }
    }

    "redirect to Email confirmation page when user is happy with the email" in new Setup {
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      when(mockSave4LaterService.saveJourneyType(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))
      when(mockEmailVerificationService.createEmailVerificationRequest(any, any, any)(any))
        .thenReturn(Future.successful(Some(EmailAlreadyVerified)))

      when(mockSave4LaterService.saveEmail(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))

      running(app) {
        val requestWithForm: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequest(POST,
            routes.VerifyChangeEmailController.verifyChangeEmail.url).withFormUrlEncodedBody(("isVerify", "true"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER

        redirectLocation(result) shouldBe Some(routes.EmailConfirmedController.show.url)
      }
    }

    "redirect to verify your email page when user is happy with the email" in new Setup {
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      when(mockSave4LaterService.saveJourneyType(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))
      when(mockSave4LaterService.saveEmail(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))

      when(mockEmailVerificationService.createEmailVerificationRequest(any, any, any)(any))
        .thenReturn(Future.successful(Some(EmailVerificationRequestSent)))

      running(app) {
        val requestWithForm: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequest(POST,
            routes.VerifyChangeEmailController.verifyChangeEmail.url).withFormUrlEncodedBody(("isVerify", "true"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.VerifyYourEmailController.show.url)

        verify(mockSave4LaterService, times(1)).saveJourneyType(any, any)(any)
        verify(mockSave4LaterService, times(1)).saveEmail(any, any)(any)
      }
    }

    "redirect to verify your email page when user is happy with the email but" +
      " email fails to store in the DB" in new Setup {
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      when(mockSave4LaterService.saveJourneyType(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))
      when(mockSave4LaterService.saveEmail(any, any)(any)).thenReturn(Future.successful(Left(BadRequest)))

      when(mockEmailVerificationService.createEmailVerificationRequest(any, any, any)(any))
        .thenReturn(Future.successful(Some(EmailVerificationRequestSent)))

      running(app) {
        val requestWithForm: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequest(POST,
            routes.VerifyChangeEmailController.verifyChangeEmail.url).withFormUrlEncodedBody(("isVerify", "true"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.VerifyYourEmailController.show.url)

        verify(mockSave4LaterService, times(1)).saveJourneyType(any, any)(any)
        verify(mockSave4LaterService, times(1)).saveEmail(any, any)(any)
      }
    }

    "redirect to check your email, problem with the service page when user is happy with the email" in new Setup {

      val errorCode = 403

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponse))

      when(mockSave4LaterService.saveJourneyType(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))

      when(mockEmailVerificationService.createEmailVerificationRequest(any, any, any)(any))
        .thenReturn(Future.successful(Some(EmailVerificationRequestFailure(errorCode, "test_body"))))

      running(app) {
        val requestWithForm: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequest(POST,
            routes.VerifyChangeEmailController.verifyChangeEmail.url).withFormUrlEncodedBody(("isVerify", "true"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER

        redirectLocation(result) shouldBe Some(routes.CheckYourEmailController.problemWithService().url)
      }
    }

    "redirect to problem with this service page when SubscriptionDisplayResponse have no email" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(subscriptionDisplayResponseWithNoEmail))

      running(app) {
        val requestWithForm: FakeRequest[AnyContentAsFormUrlEncoded] =
          fakeRequest(POST, routes.VerifyChangeEmailController.verifyChangeEmail.url)
            .withFormUrlEncodedBody(("isVerify", "false"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.VerifyChangeEmailController.problemWithService().url)
      }
    }

    "show 'there is a problem with the service' page when subscription display " +
      "response has paramValue 'FAIL' with no email for bad request form" in new Setup {

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[String])(any[HeaderCarrier]))
        .thenReturn(Future.successful(someSubscriptionDisplayResponseWithStatus))

      running(app) {
        val request = FakeRequest(POST, routes.WhatIsYourEmailController.submit.url)
          .withFormUrlEncodedBody("email" -> "invalidEmail")

        val result = route(app, request).value
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe routes.WhatIsYourEmailController.problemWithService().url
      }
    }

    "redirect to 'there is a problem with the service' page" in new Setup {

      running(app) {
        val errorHandler = app.injector.instanceOf[ErrorHandler]
        val request = fakeRequest(GET, routes.WhatIsYourEmailController.problemWithService().url)
          .withFormUrlEncodedBody("email" -> emptyString)

        val result = route(app, request).value
        status(result) shouldBe BAD_REQUEST

        contentAsString(result) shouldBe errorHandler.problemWithService()(request).toString()
      }
    }
  }

  trait Setup {
    val emailVerificationTimeStamp = "2023-2-17T9:30:47.114"
    val someSubscriptionDisplayResponse =
      SubscriptionDisplayResponse(Some("test@email.com"), Some(emailVerificationTimeStamp), None, None)

    val subscriptionDisplayResponseWithNoEmail =
      SubscriptionDisplayResponse(None, Some(emailVerificationTimeStamp), None, None)

    val someSubscriptionDisplayResponseWithNoEmailVerificationTimeStamp =
      SubscriptionDisplayResponse(Some("test@email.com"), None, None, None)

    val someSubscriptionDisplayResponseWithStatus =
      SubscriptionDisplayResponse(None, None, Some("statusText"), Some("FAIL"))

    val noneSubscriptionDisplayResponseWithStatus =
      SubscriptionDisplayResponse(None, None, Some(emptyString), Some(emptyString))

    val noneSubscriptionDisplayResponse = SubscriptionDisplayResponse(None, None, None, None)

    protected val mockSave4LaterService: Save4LaterService = mock[Save4LaterService]
    protected val mockSubscriptionDisplayConnector: SubscriptionDisplayConnector = mock[SubscriptionDisplayConnector]
    protected val mockEmailVerificationConnector: EmailVerificationConnector = mock[EmailVerificationConnector]
    protected val mockEmailVerificationService: EmailVerificationService = mock[EmailVerificationService]
    protected val mockErrorHandler: ErrorHandler = mock[ErrorHandler]

    protected val app: Application = applicationBuilder[FakeIdentifierAgentAction]()
      .overrides(inject.bind[Save4LaterService].toInstance(mockSave4LaterService),
        inject.bind[SubscriptionDisplayConnector].toInstance(mockSubscriptionDisplayConnector),
        inject.bind[EmailVerificationConnector].toInstance(mockEmailVerificationConnector),
        inject.bind[EmailVerificationService].toInstance(mockEmailVerificationService)
      ).build()

    protected val view: verify_change_email = app.injector.instanceOf[verify_change_email]

    protected def fakeRequest(method: String = emptyString,
                              path: String = emptyString): FakeRequest[AnyContentAsEmpty.type] =
      FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
        .withHeaders("X-Session-Id" -> "someSessionId")

    protected def messages(app: Application): Messages =
      app.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))
  }
}
