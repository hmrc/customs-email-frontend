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

package uk.gov.hmrc.customs.emailfrontend.services

import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import play.api.Logger
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.{EmailStatus, InternalId}
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class Save4LaterCachingConfig @Inject()(httpClient: HttpClient, appConfig: AppConfig) extends ShortLivedHttpCaching {

  override val defaultSource: String = appConfig.appName

  override val baseUri: String = appConfig.save4LaterBaseUrl

  override val domain: String = appConfig.save4LaterDomain

  override val http: HttpClient = httpClient
}

@Singleton
class EmailCacheService @Inject()(caching: Save4LaterCachingConfig, applicationCrypto: ApplicationCrypto)
  extends ShortLivedCache {

  override implicit val crypto: CompositeSymmetricCrypto = applicationCrypto.JsonCrypto

  override def shortLiveCache: ShortLivedHttpCaching = caching

  val emailKey = "email"

  val timestampKey = "timestamp"

  def saveEmail(internalId: InternalId, emailStatus: EmailStatus)
               (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] = {
    Logger.info("saving email address to save 4 later")
    cache[EmailStatus](internalId.id, emailKey, emailStatus)
  }

  def saveTimeStamp(internalId: InternalId, verifiedEmailTimestamp: DateTime)
                   (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] = {

    import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil._
    Logger.info("saving verified email time stamp to save 4 later")
    cache[DateTime](internalId.id, timestampKey, verifiedEmailTimestamp)
  }

  private def fetchTimeStamp(internalId: InternalId)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Option[DateTime]] = {
    import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil._
    Logger.info("retrieving cached timestamp from save 4 later")
    fetchAndGetEntry[DateTime](internalId.id, timestampKey)
  }

  def emailAmendmentStatus(internalId: InternalId)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[EmailAmendmentStatus] = {
    fetchTimeStamp(internalId).map {
      case Some(date) => println(s" ******************** THIS IS THE DATE $date") ; if (date.isBefore(DateTime.now.minusDays(1))) AmendmentCompleted else AmendmentInProgress
      case None => println("************************** IT NOT DETERMIND !!!!"); AmendmentNotDetermined
    }
  }

  def remove(internalId: InternalId)
            (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[HttpResponse] = {
    Logger.info("removing cached data from save 4 later")
    remove(internalId.id)
  }

  def fetchEmail(internalId: InternalId)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Option[EmailStatus]] = {
    Logger.info("retrieving cached email from save 4 later")
    fetchAndGetEntry[EmailStatus](internalId.id, emailKey)
  }
}
