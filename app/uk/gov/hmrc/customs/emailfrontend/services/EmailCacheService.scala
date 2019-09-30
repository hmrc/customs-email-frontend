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
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.crypto.{ApplicationCrypto, CompositeSymmetricCrypto}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{AmendmentInProgressController, SignOutController}
import uk.gov.hmrc.customs.emailfrontend.model.{InternalId, _}
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

  def remove(internalId: InternalId)
            (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[HttpResponse] = {
    Logger.info("removing cached data from save 4 later")
    remove(internalId.id)
  }

  def save(internalId: InternalId, cachedData: CachedData)
          (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[CacheMap] = {
    Logger.info("saving email address and timestamp to save 4 later")
    cache[CachedData](internalId.id, emailKey, cachedData)
  }

  def fetch(internalId: InternalId)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Option[CachedData]] = {
    Logger.info("retrieving email address and timestamp from save 4 later")
    fetchAndGetEntry[CachedData](internalId.id, emailKey)
  }
}

object EmailCacheService {

  implicit class EmailCacheServiceHelper(emailCacheService: EmailCacheService) {

    def emailAmendmentData(internalId: InternalId)(redirectBasedOnEmailStatus: String => Future[Result])(implicit hc: HeaderCarrier, executionContext: ExecutionContext) = {
      emailCacheService.fetch(internalId).flatMap {
        case Some(data) if data.amendmentInProgress => Future.successful(Redirect(AmendmentInProgressController.show()))
        case Some(CachedData(email, _)) => redirectBasedOnEmailStatus(email)
        case _ => Future.successful(Redirect(SignOutController.signOut()))
      }
    }
  }
}
