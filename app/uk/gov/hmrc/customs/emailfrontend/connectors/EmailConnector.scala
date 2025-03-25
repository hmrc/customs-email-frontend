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
import play.api.http.Status.SERVICE_UNAVAILABLE
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.SendEmailRequest
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class EmailConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2
) extends Logging {
  def sendEmail(to: String, templateId: String, params: Map[String, String])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[HttpResponse] = {
    val request = SendEmailRequest(Seq(to), templateId, params)

    httpClient
      .post(url"${appConfig.emailServiceUrl}")
      .withBody[SendEmailRequest](request)
      .execute[Either[UpstreamErrorResponse, HttpResponse]]
      .map {
        case Left(errResponse) => HttpResponse(errResponse.statusCode)
        case Right(value)      => value
      }
      .recover { case e =>
        logger.error(s"Call to email service failed url=${appConfig.emailServiceUrl}, exception=$e")
        HttpResponse(SERVICE_UNAVAILABLE)
      }
  }
}
