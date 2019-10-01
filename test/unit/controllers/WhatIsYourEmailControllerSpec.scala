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

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.controllers.WhatIsYourEmailController
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class WhatIsYourEmailControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val view = app.injector.instanceOf[change_your_email]
  private val verifyView = app.injector.instanceOf[what_is_your_email]
  private val mockEmailCacheService = mock[EmailCacheService]
  private val mockSubscriptionDisplayConnector = mock[SubscriptionDisplayConnector]
  private val mockEmailVerificationService = mock[EmailVerificationService]

  private val internalId = "InternalID"
  private val jsonValue = Json.toJson("emailStatus")
  private val data = Map(internalId -> jsonValue)
  private val cacheMap = CacheMap(internalId, data)
  private val someSubscriptionDisplayResponse = SubscriptionDisplayResponse(Some("test@test.com"))

  private val controller = new WhatIsYourEmailController(fakeAction, view, verifyView, mockEmailCacheService, mcc, mockSubscriptionDisplayConnector, mockEmailVerificationService)

  override protected def beforeEach(): Unit = {
    reset(mockEmailCacheService, mockSubscriptionDisplayConnector, mockEmailVerificationService)
  }

  "WhatIsYourEmailController" should {

    "have a status of SEE_OTHER for show method when email found in cache and email status is AmendmentCompleted" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(Some(DateTime.now().minusDays(2))))
      when(mockEmailCacheService.remove(meq(InternalId("internalId")))(any(), any())).thenReturn(Future.successful(HttpResponse(OK)))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/change-email-address/create")
    }

    "have a status of SEE_OTHER for show method when email found in cache and email status is AmendmentNotDetermined and email is not in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(None))
      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/change-email-address/create")

    }

    "have a status of SEE_OTHER for show method when email found in cache and email status is AmendmentNotDetermined or AmendmentComplete  and email is in cache and is verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@email"))))
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockEmailVerificationService.isEmailVerified(meq("test@email"))(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/email-address-confirmed")

    }
    "have a status of SEE_OTHER for show method when email found in cache and email status is AmendmentNotDetermined or AmendmentComplete  and email is in cache and is not verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@email"))))
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockEmailVerificationService.isEmailVerified(meq("test@email"))(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/check-email-address")

    }


    "have a status of SEE_OTHER for show method when email found in cache and Amendment status is AmendmentInProgress" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())( any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@email"))))
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(Some(DateTime.now())))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/cannot-change-email")
    }

    "have a status of SEE_OTHER for show method when Amendment status AmendmentNotDetermined or AmendmentComplete  and email not found in cache " in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(None))
      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/change-email-address/create")
    }

    "have a status of OK for create method when verified email found in response" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      when(mockEmailVerificationService.isEmailVerified(meq(someSubscriptionDisplayResponse.email.get))(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of SEE_OTHER for create method when the bookmark url is used and user already complete success amend email journey" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(Some(DateTime.now())))
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      when(mockEmailVerificationService.isEmailVerified(meq(someSubscriptionDisplayResponse.email.get))(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.create(request)
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/cannot-change-email")

    }

    "have a status of SEE_OTHER for create method when unverified email found in response" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(None))

      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      when(mockEmailVerificationService.isEmailVerified(meq(someSubscriptionDisplayResponse.email.get))(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/email-address/verify-email-address")
    }

    "have a status of Redirect for create method when email found in response" in withAuthorisedUserWithoutEori {
      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/ineligible/no-enrolment")
    }

    "have a status of OK for verify method" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(None))

      val eventualResult = controller.verify(request)

      status(eventualResult) shouldBe OK
    }
    "have a status of SEE_OTHER for verify method when the bookmark url is used and user already complete success amend email journey " in withAuthorisedUser() {
      when(mockEmailCacheService.fetchTimeStamp(any())(any(), any())).thenReturn(Future.successful(Some(DateTime.now())))

      val eventualResult = controller.verify(request)
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/cannot-change-email")

    }

    "have a status of Bad Request when no email is provided in the form" in withAuthorisedUser() {
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe BAD_REQUEST
    }

    "have a status of Bad Request when the email is invalid" in withAuthorisedUser() {
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "invalidEmail")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe BAD_REQUEST
    }


    "have a status of OK when the email is valid" in withAuthorisedUser() {
      when(mockEmailCacheService.saveEmail(any(), any())(any(), any())).thenReturn(Future.successful(cacheMap))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "valid@email.com")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/check-email-address")
    }

    "have a status of Bad Request for verifySubmit method when no email is provided in the form" in withAuthorisedUser() {
      val eventualResult = controller.verifySubmit(request)

      status(eventualResult) shouldBe BAD_REQUEST
    }

    "have a status of OK for verifyEmail method when the email is valid" in withAuthorisedUser() {
      when(mockEmailCacheService.saveEmail(any(), any())(any(), any())).thenReturn(Future.successful(cacheMap))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "valid@email.com")
      val eventualResult = controller.verifySubmit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/check-email-address")
    }
  }
}
