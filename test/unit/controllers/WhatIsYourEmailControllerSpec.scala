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
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Request}
import play.api.test.Helpers._
import uk.gov.hmrc.customs.emailfrontend.controllers.WhatIsYourEmailController
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.customs.emailfrontend.services.EmailCacheService
import uk.gov.hmrc.customs.emailfrontend.views.html.what_is_your_email
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class WhatIsYourEmailControllerSpec extends ControllerSpec {

  private val view = app.injector.instanceOf[what_is_your_email]
  private val mockEmailCacheService = mock[EmailCacheService]

  val internalId = "InternalID"
  val jsonValue = Json.toJson("emailStatus")
  val data = Map(internalId -> jsonValue)
  val cacheMap = CacheMap(internalId, data)

  private val controller = new WhatIsYourEmailController(fakeAction, view, mockEmailCacheService, mcc)

  "WhatIsYourEmailController" should {

    "have a status of SEE_OTHER for show method when email found in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(Some(EmailStatus("test@email"))))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/check-email-address")
    }

    "have a status of SEE_OTHER for show method when email not found in cache" in withAuthorisedUser() {
      when(mockEmailCacheService.fetchEmail(any())(any(), any())).thenReturn(Future.successful(None))

      val eventualResult = controller.show(request)

      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/customs-email-frontend/email-address/create")
    }

    "have a status of OK for create method" in withAuthorisedUser() {
      val eventualResult = controller.create(request)

      status(eventualResult) shouldBe OK
    }

    "have a status of Bad Request when no email is provided" in withAuthorisedUser() {
      val request: Request[AnyContentAsFormUrlEncoded] = requestWithForm("email" -> "")
      val eventualResult = controller.submit(request)

      status(eventualResult) shouldBe BAD_REQUEST
    }

    "have a status of Bad Request when the email is invalid" in withAuthorisedUser() {
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
  }
}
