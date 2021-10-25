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

package uk.gov.hmrc.customs.emailfrontend.controllers

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{eq => meq}
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.config.ErrorHandler
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, InternalId, ReferrerName}
import uk.gov.hmrc.customs.emailfrontend.services._
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException}

import scala.concurrent.Future

class EmailConfirmedControllerSpec extends SpecBase {

  trait Setup {

    protected val mockSave4LaterService: Save4LaterService = mock[Save4LaterService]
    protected val mockCustomsDataStoreService: CustomsDataStoreService = mock[CustomsDataStoreService]
    protected val mockEmailVerificationService: EmailVerificationService = mock[EmailVerificationService]
    protected val mockUpdateVerifiedEmailService: UpdateVerifiedEmailService = mock[UpdateVerifiedEmailService]
    protected val mockDateTimeService: DateTimeService = mock[DateTimeService]
    protected val testDateTime: DateTime = DateTime.parse("2021-01-01T11:11:11.111Z")

    protected val app: Application = applicationBuilder[FakeIdentifierAgentAction]()
      .overrides(
        inject.bind[Save4LaterService].toInstance(mockSave4LaterService),
        inject.bind[CustomsDataStoreService].toInstance(mockCustomsDataStoreService),
        inject.bind[EmailVerificationService].toInstance(mockEmailVerificationService),
        inject.bind[UpdateVerifiedEmailService].toInstance(mockUpdateVerifiedEmailService),
        inject.bind[DateTimeService].toInstance(mockDateTimeService)
      ).build()

    private val view = app.injector.instanceOf[email_confirmed]

    protected val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]


  }

  "EmailConfirmedController" should {
    "return OK " when {
      "email found in cache, email is verified and update verified email is successful" in new Setup() {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockDateTimeService.nowUtc()).thenReturn(testDateTime)

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(
          meq(None),
          meq("abc@def.com"),
          meq("fakeEori"),
          meq(testDateTime))(any)
        ).thenReturn(Future.successful(Some(true)))

        when(mockSave4LaterService.saveEmail(meq(InternalId("fakeInternalId")), any)(any))
          .thenReturn(Future.successful(()))

        when(mockSave4LaterService.fetchReferrer(meq(InternalId("fakeInternalId")))(any, any))
          .thenReturn(Future.successful(Some(ReferrerName("abc", "/xyz"))))

        when(mockCustomsDataStoreService.storeEmail(meq(EnrolmentIdentifier("EORINumber", "fakeEori")), meq("abc@def.com"), meq(testDateTime))(any[HeaderCarrier]))
          .thenReturn(Future.successful(HttpResponse(OK, "")))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe OK
        }

      }

    }

    "return REDIRECT to confirm email page" when {
      "email found in cache but email is not verified" in new Setup {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.VerifyYourEmailController.show().url
        }

      }

      "when email found in cache but isEmailVerified failed" in new Setup {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))
        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any)).thenReturn(Future.successful(None))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.VerifyYourEmailController.show().url
        }

      }

    }

    "return REDIRECT to sign-out page" when {
      "email not found in cache" in new Setup {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(None))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.SignOutController.signOut().url
        }
      }
    }

    "return REDIRECT to cannot change email page" when {
      "user retries the same request(user click back on successful request or refreshes the browser)" in new Setup {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", Some(DateTime.now())))))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.AmendmentInProgressController.show().url
        }
      }
    }

    "return REDIRECT to problem page" when {
      "email found in cache, email is verified and update verified email is successful but saving timestamp fails" in new Setup {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(meq("abc@def.com"))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockDateTimeService.nowUtc()).thenReturn(testDateTime)

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(meq(None), meq("abc@def.com"), meq("fakeEori"), meq(testDateTime))(any))
          .thenReturn(Future.successful(Some(true)))

        when(mockSave4LaterService.saveEmail(meq(InternalId("fakeInternalId")), any)(any))
          .thenReturn(Future.failed(new InternalServerException("")))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.EmailConfirmedController.problemWithService().url
        }
      }

      "save email is failed with Error 400 or 500" in new Setup {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(any)(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockDateTimeService.nowUtc()).thenReturn(testDateTime)

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(meq(None), meq("abc@def.com"), meq("fakeEori"), meq(testDateTime))(any))
          .thenReturn(Future.successful(None))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.EmailConfirmedController.problemWithService().url
        }
      }

      "save email returns 200 with no form bundle id param" in new Setup {
        when(mockSave4LaterService.fetchEmail(any)(any, any))
          .thenReturn(Future.successful(Some(EmailDetails(None, "abc@def.com", None))))

        when(mockEmailVerificationService.isEmailVerified(any)(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(true)))

        when(mockDateTimeService.nowUtc()).thenReturn(testDateTime)

        when(mockUpdateVerifiedEmailService.updateVerifiedEmail(meq(None), meq("abc@def.com"), meq("fakeEori"), meq(testDateTime))(any))
          .thenReturn(Future.successful(Some(false)))

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.show().url)
          val result = route(app, requestWithForm).value
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get shouldBe routes.EmailConfirmedController.problemWithService().url
        }

      }
    }

    "show problem with service page" when {

      "when user is redirected to .problemWithService" in new Setup {

        running(app) {
          val requestWithForm = FakeRequest(GET, routes.EmailConfirmedController.problemWithService().url)
            .withFormUrlEncodedBody("email" -> "")
          val result = route(app, requestWithForm).value
          status(result) shouldBe BAD_REQUEST
          contentAsString(result) shouldBe errorHandler.problemWithService()(requestWithForm).toString()
        }

      }

    }
  }

}
