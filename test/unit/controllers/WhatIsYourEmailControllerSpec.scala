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
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse}

import scala.concurrent.Future

class WhatIsYourEmailControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val view = app.injector.instanceOf[change_your_email]
  private val verifyView = app.injector.instanceOf[what_is_your_email]
  private val problemWithServiceView = app.injector.instanceOf[problem_with_this_service]
  private val mockEmailCacheService = mock[EmailCacheService]
  private val mockSubscriptionDisplayConnector = mock[SubscriptionDisplayConnector]
  private val mockEmailVerificationService = mock[EmailVerificationService]

  private val internalId = "InternalID"
  private val jsonValue = Json.toJson("emailStatus")
  private val data = Map(internalId -> jsonValue)
  private val cacheMap = CacheMap(internalId, data)
  private val someSubscriptionDisplayResponse = SubscriptionDisplayResponse(Some("test@test.com"))

  private val controller = new WhatIsYourEmailController(fakeAction, view, verifyView, problemWithServiceView, mockEmailCacheService, mcc, mockSubscriptionDisplayConnector, mockEmailVerificationService)

  override protected def beforeEach(): Unit = {
    reset(mockEmailCacheService, mockSubscriptionDisplayConnector, mockEmailVerificationService)
  }

  "WhatIsYourEmailController" should {

    "have a status of SEE_OTHER for show method when email found in cache and email status is AmendmentCompleted" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", Some(DateTime.now().minusDays(2))))))
      when(mockEmailCacheService.remove(meq(InternalId("internalId")))(any(), any())).thenReturn(Future.successful(HttpResponse(OK)))
      when(mockEmailVerificationService.isEmailVerified(meq("test@email"))(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/change-email-address/create")
    }

    "have a status of SEE_OTHER for show method when email found in cache and email status is AmendmentNotDetermined and email is not in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/change-email-address/create")

    }

    "have a status of SEE_OTHER for show method when email found in cache with no timestamp and email is verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", None))))
      when(mockEmailVerificationService.isEmailVerified(meq("test@email"))(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/email-address-confirmed")

    }
    "have a status of SEE_OTHER for show method when email found in cache with no timestamp and email is not verified" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", None))))
      when(mockEmailVerificationService.isEmailVerified(meq("test@email"))(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/check-email-address")
    }

    "have a status of SEE_OTHER for show method when email found in cache with timestamp for AmendmentInProgress" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())( any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", Some(DateTime.now())))))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/cannot-change-email")
    }

    "have a status of SEE_OTHER for show method email is not found in cache " in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))
      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/change-email-address/create")
    }

    "have a status of OK for create method when verified email found in subscription display response" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      when(mockEmailVerificationService.isEmailVerified(meq(someSubscriptionDisplayResponse.email.get))(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of SEE_OTHER for create method when unverified email found in subscription display response" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      when(mockEmailVerificationService.isEmailVerified(meq(someSubscriptionDisplayResponse.email.get))(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/email-address/verify-email-address")
    }

    "have a status of SEE_OTHER for create method when email found in cache with no timestamp" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", None))))
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))

      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of SEE_OTHER for create method when the bookmark url is used and user already completed success amend email journey" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", Some(DateTime.now())))))
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      when(mockEmailVerificationService.isEmailVerified(meq(someSubscriptionDisplayResponse.email.get))(any[HeaderCarrier])).thenReturn(Future.successful(Some(true)))

      val eventualResult = controller.create(request)
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/cannot-change-email")
    }

    "have a status of OK for create method when unverified email found in subscription display response" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      when(mockEmailVerificationService.isEmailVerified(meq(someSubscriptionDisplayResponse.email.get))(any[HeaderCarrier])).thenReturn(Future.successful(Some(false)))

      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/email-address/verify-email-address")
    }

    "show 'there is a problem with the service' page when subscription display is failed" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.failed(new HttpException("Failed", BAD_REQUEST)))

      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe OK
      contentAsString(eventualResult).contains("Sorry, there is a problem with the service") shouldBe true
    }

    "have a status of Redirect for create method for unauthorised user" in withAuthorisedUserWithoutEori {
      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/ineligible/no-enrolment")
    }

    "have a status of OK for verify method" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(None))

      val eventualResult = controller.verify(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of OK for verify method when email found in cache with no timestamp" in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", None))))

      val eventualResult = controller.verify(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of SEE_OTHER for verify method when the bookmark url is used and user already complete success amend email journey " in withAuthorisedUser() {
      when(mockEmailCacheService.fetch(any())(any(), any())).thenReturn(Future.successful(Some(EmailDetails("test@email", Some(DateTime.now())))))

      val eventualResult = controller.verify(request)
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/cannot-change-email")
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
      when(mockEmailCacheService.save(any(), any())(any(), any())).thenReturn(Future.successful(cacheMap))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "valid@email.com")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/check-email-address")
    }

    "show 'there is a problem with the service' page when subscription display is failed for submit" in withAuthorisedUser() {
      when(mockSubscriptionDisplayConnector.subscriptionDisplay(any[Eori])(any[HeaderCarrier])).thenReturn(Future.failed(new HttpException("Failed", BAD_REQUEST)))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe OK
      contentAsString(eventualResult).contains("Sorry, there is a problem with the service") shouldBe true
    }

    "have a status of Bad Request for verifySubmit method when no email is provided in the form" in withAuthorisedUser() {
      val eventualResult = controller.verifySubmit(request)

      status(eventualResult) shouldBe BAD_REQUEST
    }

    "have a status of OK for verifyEmail method when the email is valid" in withAuthorisedUser() {
      when(mockEmailCacheService.save(any(), any())(any(), any())).thenReturn(Future.successful(cacheMap))

      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "valid@email.com")
      val eventualResult = controller.verifySubmit(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/check-email-address")
    }
  }
}
