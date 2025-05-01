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

import play.api.http.Status
import play.api.libs.ws.writeableOf_JsValue
import play.api.{Logger, LoggerLike}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.EmailRequest
import uk.gov.hmrc.customs.emailfrontend.services.MetricsReporterService
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailThrottlerConnector @Inject() (http: HttpClientV2, metricsReporter: MetricsReporterService)(implicit
  appConfig: AppConfig,
  ec: ExecutionContext
) {

  val log: LoggerLike = Logger(this.getClass)

  def sendEmail(request: EmailRequest)(implicit hc: HeaderCarrier): Future[Boolean] =
    metricsReporter.withResponseTimeLogging(s"email.post.${request.templateId}") {

      http
        .post(url"${appConfig.sendEmailEndpoint}")
        .withBody[EmailRequest](request)
        .execute[HttpResponse]
        .collect {
          case response if response.status == Status.ACCEPTED =>
            log.info(s"successfuly sent email notification for ${request.templateId}")
            true

          case response =>
            log.error(s"Send email failed with status - ${response.status}")
            false

        }
        .recover { case ex: Throwable =>
          log.error(s"Send email threw an exception - ${ex.getMessage}")
          false
        }
    }
}
