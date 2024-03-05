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

import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.{EmailAlreadyVerified, EmailVerificationRequestSent}
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.customs.emailfrontend.services.{EmailVerificationService, Save4LaterService}
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString

import java.time.Instant
import scala.concurrent.Future

class ChangingYourEmailControllerSpec extends SpecBase {

  "ChangingYourEmailController" should {
    "have a status of OK when email found in cache" in new Setup {

      running(app) {
        val request = FakeRequest(GET, routes.ChangingYourEmailController.show.url)
        val result = route(app, request).value

        status(result) shouldBe OK
      }
    }

    "have a status of SEE_OTHER when email not found in cache on show" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(None))

      running(app) {

        val requestWithForm = FakeRequest(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.SignOutController.signOut.url
      }
    }

    "have a status of SEE_OTHER when email not found in cache on submit" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(None))

      running(app) {

        val requestWithForm = FakeRequest(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.SignOutController.signOut.url
      }
    }

    "have a status of BAD_REQUEST when no selection is provided" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(None))

      running(app) {

        val requestWithForm = FakeRequest(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.SignOutController.signOut.url
      }
    }

    "have a status of SEE_OTHER when no is selected" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(None))

      running(app) {

        val requestWithForm = fakeRequestWithCsrf(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", "false"))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
      }
    }

    "have a status of SEE_OTHER when user clicks back on the successful request or user " +
      "already complete bookmarked request within 2 hours" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(None))

      running(app) {

        val requestWithForm = fakeRequestWithCsrf(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", "false"))
        val result = route(app, requestWithForm).value
        status(result) shouldBe SEE_OTHER
      }
    }

    "redirect to 'there is a problem with the service' page" in new Setup {
      running(app) {

        val request = FakeRequest(routes.CheckYourEmailController.problemWithService)
        val result = route(app, request).value

        status(result) shouldBe BAD_REQUEST
        contentAsString(result) shouldBe errorHandler.problemWithService()(request).toString()
      }
    }
  }

  "VerifyChangeEmailController on submit with yes selected" should {
    "redirect to Email Confirmed page when email is already verified" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(emailDetails)))

      when(mockEmailVerificationService.createEmailVerificationRequest(any, any, any)(any)).thenReturn(
        Future.successful(Some(EmailAlreadyVerified)))

      when(mockSave4LaterService.saveEmail(any, any)(any)).thenReturn(Future.successful(Right((): Unit)))

      running(app) {

        val requestWithForm = fakeRequestWithCsrf(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.EmailConfirmedController.show.url)
      }
    }

    "redirect to Verify Your Email page when email yet not verified" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(emailDetails)))

      when(mockEmailVerificationService.createEmailVerificationRequest(any, any, any)(any)).thenReturn(
        Future.successful(Some(EmailVerificationRequestSent)))

      running(app) {

        val requestWithForm = fakeRequestWithCsrf(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.VerifyYourEmailController.show.url)
      }
    }

    "show 'there is a problem with service' page when createEmailVerificationRequest failed" in new Setup {

      when(mockSave4LaterService.fetchEmail(any)(any))
        .thenReturn(Future.successful(Some(emailDetails)))

      when(mockEmailVerificationService.createEmailVerificationRequest(any, any, any)(any)).thenReturn(
        Future.successful(None))

      running(app) {

        val requestWithForm = fakeRequestWithCsrf(POST, routes.ChangingYourEmailController.submit.url)
          .withFormUrlEncodedBody(("isYes", emptyString))

        val result = route(app, requestWithForm).value

        status(result) shouldBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.CheckYourEmailController.problemWithService.url)
      }
    }
  }

  trait Setup {

    val emailDetails: EmailDetails = EmailDetails(
      currentEmail = Some("test@test.com"),
      newEmail = "test_new@test.com",
      timestamp = Some(Instant.now()))

    protected val mockSave4LaterService: Save4LaterService = mock[Save4LaterService]
    protected val mockEmailVerificationService: EmailVerificationService = mock[EmailVerificationService]

    protected val app: Application = applicationBuilder[FakeIdentifierAgentAction]()
      .overrides(
        inject.bind[Save4LaterService].toInstance(mockSave4LaterService),
        inject.bind[EmailVerificationService].toInstance(mockEmailVerificationService)
      )
      .build()

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

    protected val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  }
}
