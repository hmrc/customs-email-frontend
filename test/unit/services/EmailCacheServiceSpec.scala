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

import org.joda.time.DateTime
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
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, Save4LaterCachingConfig}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EmailCacheServiceSpec extends PlaySpec with ScalaFutures with MockitoSugar with BeforeAndAfter {

  private val mockEmailCachingConfig = mock[Save4LaterCachingConfig]
  private val mockApplicationCrypto = mock[ApplicationCrypto]

  private val internalId = InternalId("internalID")
  private val emailStatus = EmailStatus("test@test.com")
  private val jsonValue = Json.toJson(emailStatus)
  private val data = Map(internalId.id -> jsonValue)
  private val timestamp = DateTimeUtil.dateTime
  private val cacheMap = CacheMap(internalId.id, data)
  private val successResponse = HttpResponse(OK)

  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]

  private val service = new EmailCacheService(mockEmailCachingConfig, mockApplicationCrypto)

  before {
    reset(mockEmailCachingConfig, mockApplicationCrypto)
  }

  "EmailCacheService for ShortLivedCache" should {

    "save Email" in {
      when(mockEmailCachingConfig.cache(meq(internalId.id), meq("email"), meq(Protected(emailStatus)))(any[HeaderCarrier],
        any(), any[ExecutionContext])).thenReturn(Future.successful(cacheMap))

      val cache: CacheMap = service.saveEmail(internalId, emailStatus).futureValue

      cache mustBe cacheMap
    }


    "fetch Email" in {
      when(mockEmailCachingConfig.fetchAndGetEntry[Protected[EmailStatus]](meq(internalId.id), meq("email"))(any[HeaderCarrier], any(), any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Protected(emailStatus))))

      val cachedEmailStatus = service.fetchEmail(internalId).futureValue

      cachedEmailStatus mustBe Some(emailStatus)
    }

    "save timestamp" in {
      when(mockEmailCachingConfig.cache(meq(internalId.id), meq("timestamp"), meq(Protected(timestamp)))(any[HeaderCarrier],
        any(), any[ExecutionContext])).thenReturn(Future.successful(cacheMap))

      val cache: CacheMap = service.saveTimeStamp(internalId, timestamp).futureValue

      cache mustBe cacheMap
    }


    "remove data" in {
      when(mockEmailCachingConfig.remove(meq(internalId.id))(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(successResponse))

      val cache: HttpResponse = service.remove(internalId).futureValue

      cache mustBe successResponse
    }

    "fetch timestamp and return verificationCompleted" in {
      when(mockEmailCachingConfig.fetchAndGetEntry[Protected[DateTime]](meq(internalId.id), meq("timestamp"))(any[HeaderCarrier], any(), any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Protected(timestamp.minusDays(2)))))

      val cacheVerificationStatus = service.emailVerificationStatus(internalId).futureValue

      cacheVerificationStatus mustBe VerificationCompleted
    }

    "fetch timestamp and return verificationInProgress" in {
      when(mockEmailCachingConfig.fetchAndGetEntry[Protected[DateTime]](meq(internalId.id), meq("timestamp"))(any[HeaderCarrier], any(), any[ExecutionContext]))
        .thenReturn(Future.successful(Some(Protected(timestamp))))

      val cacheVerificationStatus = service.emailVerificationStatus(internalId).futureValue

      cacheVerificationStatus mustBe VerificationInProgress
    }

    "fetch timestamp and return verificationNotDetermined" in {
      when(mockEmailCachingConfig.fetchAndGetEntry[Protected[DateTime]](meq(internalId.id), meq("timestamp"))(any[HeaderCarrier], any(), any[ExecutionContext]))
        .thenReturn(Future.successful(None))

      val cacheVerificationStatus = service.emailVerificationStatus(internalId).futureValue

      cacheVerificationStatus mustBe VerificationNotDetermined
    }
  }
}
