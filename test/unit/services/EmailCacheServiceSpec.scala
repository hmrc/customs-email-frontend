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

package unit.services

import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.OK
import play.api.libs.json._
import uk.gov.hmrc.crypto.{ApplicationCrypto, Protected}
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, Save4LaterCachingConfig}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EmailCacheServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar with BeforeAndAfter {

  private val mockEmailCachingConfig = mock[Save4LaterCachingConfig]
  private val mockApplicationCrypto = mock[ApplicationCrypto]

  val internalId = "InternalID"
  val emailStatus = EmailStatus("test@test.com")
  val jsonValue = Json.toJson(emailStatus)
  val data = Map(internalId -> jsonValue)
  val timestamp = DateTimeUtil.dateTime
  val cacheMap = CacheMap(internalId, data)
  val successResponse = HttpResponse(OK)

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  val service = new EmailCacheService(mockEmailCachingConfig, mockApplicationCrypto)

  before {
    reset(mockEmailCachingConfig, mockApplicationCrypto)
  }

  "EmailCacheService for ShortLivedCache" should {

    "save Email" in {
      when(mockEmailCachingConfig.cache(meq(internalId), meq("email"), meq(Protected(emailStatus)))(any[HeaderCarrier],
        any(), any[ExecutionContext])).thenReturn(Future.successful(cacheMap))

      val cache: CacheMap = service.saveEmail(Some(internalId), emailStatus).futureValue

      cache mustBe cacheMap
    }

    "save Email throws IllegalStateException" in {
      val internalId = None
      val status = intercept[IllegalStateException] {
        service.saveEmail(internalId, emailStatus)
      }

      status.getMessage mustBe "Auth InternalId Missing"

      verifyNoMoreInteractions(mockEmailCachingConfig)
    }

    "fetch Email" in {
      when(mockEmailCachingConfig.fetchAndGetEntry[Protected[EmailStatus]](meq(internalId), meq("email"))(any[HeaderCarrier], any(), any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Protected(emailStatus))))

      val cachedEmailStatus = service.fetchEmail(Some(internalId)).futureValue

      cachedEmailStatus mustBe Some(emailStatus)
    }

    "fetch Email throws IllegalStateException" in {
      val internalId = None
      val cachedEmailStatus = intercept[IllegalStateException] {
        service.fetchEmail(internalId)
      }

      cachedEmailStatus.getMessage mustBe "Auth InternalId Missing"

      verifyNoMoreInteractions(mockEmailCachingConfig)
    }

    "save timestamp" in {
      when(mockEmailCachingConfig.cache(meq(internalId), meq("timestamp"), meq(Protected(timestamp)))(any[HeaderCarrier],
        any(), any[ExecutionContext])).thenReturn(Future.successful(cacheMap))

      val cache: CacheMap = service.saveTimeStamp(Some(internalId), timestamp).futureValue

      cache mustBe cacheMap
    }

    "save timestamp throws IllegalStateException" in {
      val internalId = None
      val status = intercept[IllegalStateException] {
        service.saveTimeStamp(internalId, timestamp)
      }

      status.getMessage mustBe "Auth InternalId Missing"

      verifyNoMoreInteractions(mockEmailCachingConfig)
    }

    "remove data" in {
      when(mockEmailCachingConfig.remove(meq(internalId))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(successResponse))

      val cache: HttpResponse = service.remove(Some(internalId)).futureValue

      cache mustBe successResponse
    }

    "remove throws IllegalStateException" in {
      val internalId = None
      val status = intercept[IllegalStateException] {
        service.remove(internalId)
      }

      status.getMessage mustBe "Auth InternalId Missing"

      verifyNoMoreInteractions(mockEmailCachingConfig)
    }
  }
}
