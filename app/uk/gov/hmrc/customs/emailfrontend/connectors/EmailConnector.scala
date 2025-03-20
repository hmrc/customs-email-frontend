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

import play.api.Logging
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.SendEmailRequest
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.mvc.Results.InternalServerError
import uk.gov.hmrc.http.UpstreamErrorResponse.Upstream5xxResponse

import scala.util.control.NonFatal

class EmailConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2,
  servicesConfig: ServicesConfig
) extends Logging {
  def sendEmail(to: String, templateId: String, params: Map[String, String])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val body = Json.toJson(SendEmailRequest(Seq(to), templateId, params))

    httpClient
      .post(url"${appConfig.emailServiceUrl}")
      .withBody(body)
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Left(err)    =>
          throw err
        case Right(value) => value
      }
      .recover { case e =>
        logger.error(
          s"Call to email service failed url=" +
            s"${appConfig.emailServiceUrl}, exception=$e"
        )
        HttpResponse(INTERNAL_SERVER_ERROR)
      }
  }
}
