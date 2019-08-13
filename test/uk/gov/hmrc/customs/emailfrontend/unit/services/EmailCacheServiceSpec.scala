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

package uk.gov.hmrc.customs.emailfrontend.unit.services

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{Writes, _}
import play.api.mvc.{AnyContent, Request}
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, Save4LaterCachingConfig}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EmailCacheServiceSpec extends PlaySpec
  with ScalaFutures
  with MockitoSugar
  with BeforeAndAfter {

  private val mockEmailCachingConfig = mock[Save4LaterCachingConfig]
  private val mockApplicationCrypto = mock[ApplicationCrypto]

  val internalId = "InternalID"
  val emailStatus = EmailStatus("test@test.com")
  val jsonValue = Json.toJson(emailStatus)
  val data = Map(internalId -> jsonValue)
  val cacheMap = CacheMap(internalId,data)


  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  implicit val rq: Request[AnyContent] = mock[Request[AnyContent]]



  val service =  Mockito.spy( new EmailCacheService(mockEmailCachingConfig,mockApplicationCrypto) )

  import org.mockito.Mockito

  before {
    reset(mockEmailCachingConfig,mockApplicationCrypto)
  }

  "EmailCacheService for ShortLivedCache" should {

    "save Email" in {
      Mockito.doReturn(Future.successful(cacheMap),Future.successful(cacheMap))
        .when(service.asInstanceOf[ShortLivedCache]).
        cache(any[String],any[String],any[EmailStatus])(any[HeaderCarrier],any[Writes[EmailStatus]],any[ExecutionContext])

      val cache: CacheMap =  service.saveEmail(Some(internalId),emailStatus).futureValue

      cache mustBe cacheMap

    }
    "save Email throw IllegalStateException" in {
      Mockito.doReturn(Future.successful(cacheMap),Future.successful(cacheMap))
        .when(service.asInstanceOf[ShortLivedCache]).
        cache(any[String],any[String],any[EmailStatus])(any[HeaderCarrier],any[Writes[EmailStatus]],any[ExecutionContext])
      val status = intercept[IllegalStateException] {
        service.saveEmail(None, emailStatus).futureValue
      }
      status.getMessage mustBe "Auth InternalId Missing"

    }

    "fetch Email" in {
      Mockito.doReturn(Future.successful(Some(emailStatus)), Future.successful(Some(emailStatus)))
        .when(service.asInstanceOf[ShortLivedCache]).
        fetchAndGetEntry(any[String],any[String])(any[HeaderCarrier],any[Reads[EmailStatus]],any[ExecutionContext])

      val  cachedEmailStatus =  service.fetchEmail(Some(internalId)).futureValue

      cachedEmailStatus mustBe Some(emailStatus)
    }

    "fetch Email throw IllegalStateException" in {
      Mockito.doReturn(Future.successful(Some(emailStatus)), Future.successful(Some(emailStatus)))
        .when(service.asInstanceOf[ShortLivedCache]).
        fetchAndGetEntry(any[String],any[String])(any[HeaderCarrier],any[Reads[EmailStatus]],any[ExecutionContext])
      val cachedEmailStatus =  intercept[IllegalStateException] {
        service.fetchEmail(None).futureValue
      }
      cachedEmailStatus.getMessage mustBe "Auth InternalId Missing"
    }

  }


}