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
import play.api.Logger
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.EmailStatus
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache, ShortLivedHttpCaching}
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

  def saveEmail(internalId: Option[String], emailStatus: EmailStatus)
               (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] = {
    val id = internalId.getOrElse(throw new IllegalStateException("Auth InternalId Missing"))
    Logger.info("saving email address to save 4 later")
    cache[EmailStatus](id, emailKey, emailStatus)
  }

  def fetchEmail(internalId: Option[String])(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Option[EmailStatus]] = {
    val id = internalId.getOrElse(throw new IllegalStateException("Auth InternalId Missing"))
    Logger.info("calling save 4 later to retrieve email")
    fetchAndGetEntry[EmailStatus](id, emailKey)
  }
}
