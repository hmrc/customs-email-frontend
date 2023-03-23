/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.connectors

import play.api.http.Status.OK
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionCacheConnector @Inject()(httpClient: HttpClient,
                                      appConfig: AppConfig)(implicit executionContext: ExecutionContext) {

  def retrieveSession(id: String, linkId: String)(implicit hc: HeaderCarrier): Future[Option[HttpResponse]] =
    httpClient.GET(
      appConfig.customsSessionCacheUrl + s"/email-link/$id/$linkId"
    ).map(Some(_)).recover { case _ => None }

  def removeSession(id: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    httpClient.DELETE[HttpResponse](
      appConfig.customsSessionCacheUrl + "/remove/" + id
    ).map(_.status == OK).recover { case _ => false }
}