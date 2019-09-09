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

import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito.{times, verify, _}
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.controllers.EmailConfirmedController
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.customs.emailfrontend.services.{CustomsDataStoreService, EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future


class EmailConfirmedControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val view = app.injector.instanceOf[email_confirmed]
  private val mockEmailCacheService = mock[EmailCacheService]
  private val mockCustomsDataStoreService = mock[CustomsDataStoreService]
  private val mockEmailVerificationService = mock[EmailVerificationService]

  private val controller = new EmailConfirmedController(fakeAction, view, mockCustomsDataStoreService, mockEmailCacheService, mockEmailVerificationService, mcc)

  override protected def beforeEach(): Unit = {
    reset(mockCustomsDataStoreService, mockEmailVerificationService)
  }

  "EmailConfirmedController" should {
    "have a status of OK when email found in cache and email is verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("abc@def.com"))))
      when(mockCustomsDataStoreService.storeEmail(meq(EnrolmentIdentifier("EORINumber", "GB1234567890")), meq("abc@def.com"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK)))
      when(mockEmailVerificationService.isEmailVerified(any())(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.show(request)
      status(eventualResult) shouldBe OK
    }

    "have a status of SEE_OTHER when email found in cache but email is not verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("abc@def.com"))))
      when(mockCustomsDataStoreService.storeEmail(meq(EnrolmentIdentifier("EORINumber", "GB1234567890")), meq("abc@def.com"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(HttpResponse(OK)))
      when(mockEmailVerificationService.isEmailVerified(any())(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

      val eventualResult = controller.show(request)
      status(eventualResult) shouldBe SEE_OTHER

      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/confirm-email-address")
    }

    "have a status of SEE_OTHER for show method when email not found in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/signout")
    }

    "have a status of OK for user with no enrolments" in withAuthorisedUserWithoutEnrolments {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("abc@def.com"))))
      when(mockEmailVerificationService.isEmailVerified(any())(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.show(request)
      status(eventualResult) shouldBe OK

      verify(mockCustomsDataStoreService, times(0)).storeEmail(any(), any())(any[HeaderCarrier])
    }
  }
}
