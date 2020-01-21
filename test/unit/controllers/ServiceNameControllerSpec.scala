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

import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.customs.emailfrontend.controllers.ServiceNameController
import uk.gov.hmrc.customs.emailfrontend.model.{InternalId, ReferrerName}
import uk.gov.hmrc.customs.emailfrontend.services.EmailCacheService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.{ExecutionContext, Future}

class ServiceNameControllerSpec extends ControllerSpec {

  private val mockEmailCacheService = mock[EmailCacheService]
  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private val internalId = "internalID"
  private val cacheMap: CacheMap = CacheMap(internalId, Map.empty)

  private val controller = new ServiceNameController(fakeAction, appConfig, mockEmailCacheService, mcc)

  "ServiceNameController" should {
    "redirect to chance-email-address page and store the referred service name in the cache when parameter found in the url" in withAuthorisedUser() {
      when(mockEmailCacheService.saveReferrer(meq(InternalId("internalId")), meq(ReferrerName("customs-finance", "/finance")))(any[HeaderCarrier], any[ExecutionContext])).
        thenReturn(Future.successful(cacheMap))

      val request = FakeRequest("GET", "/").withCSRFToken
      val eventualResult = controller.show("customs-finance")(request)
      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/change-email-address")
    }

    "redirect to problem-with-service page when service name is not found in the url" in withAuthorisedUser() {
      when(mockEmailCacheService.saveReferrer(any(), meq(ReferrerName("", "")))(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(cacheMap))

      val request = FakeRequest("GET", "/").withCSRFToken
      val eventualResult = controller.show("")(request)
      status(eventualResult) shouldBe SEE_OTHER
      redirectLocation(eventualResult).value should endWith("/manage-email-cds/change-email-address/problem-with-this-service")
    }
  }
}
