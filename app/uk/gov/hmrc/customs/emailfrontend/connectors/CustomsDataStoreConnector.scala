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

import org.joda.time.DateTime
import play.api.Logging
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.{Eori, UpdateEmail}
import uk.gov.hmrc.http._
import javax.inject.{Inject, Singleton}
import play.api.http.Status._
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import uk.gov.hmrc.http.HttpReads.Implicits._

@Singleton
class CustomsDataStoreConnector @Inject()(appConfig: AppConfig,
                                          httpClient: HttpClient,
                                          audit: Auditable)(implicit ec: ExecutionContext) extends Logging {

  def storeEmailAddress(eori: Eori, email: String,
                        timestamp: DateTime)
                       (implicit hc: HeaderCarrier): Future[Either[HttpErrorResponse, HttpResponse]] = {

    val request = UpdateEmail(eori, email, timestamp)

    auditRequest("DataStoreEmailRequestSubmitted", Map(
      "eori number" -> eori.id, "emailAddress" -> email, "timestamp" -> timestamp.toString()))

    httpClient.POST[UpdateEmail, HttpResponse](
        appConfig.customsDataStoreUrl, request, Seq(CONTENT_TYPE -> MimeTypes.JSON))
      .map { response =>
        auditResponse("DataStoreResponseReceived", response, appConfig.customsDataStoreUrl)
        response.status match {
          case NO_CONTENT =>
            logger.debug("CustomsDataStore: data store request is successful")
            Right(response)

          case _ =>
            logger.warn(s"CustomsDataStore: data store request is failed with status ${response.status}")
            Left(BadRequest)
        }
      }.recover {
        case _: BadRequestException | UpstreamErrorResponse(_, BAD_REQUEST, _, _) => Left(BadRequest)

        case _: InternalServerException | UpstreamErrorResponse(
          _, INTERNAL_SERVER_ERROR, _, _) => Left(ServiceUnavailable)

        case NonFatal(e) =>
          logger.error(s"Call to data stored failed url=" +
            s"${appConfig.customsDataStoreUrl}, exception=$e"); Left(UnhandledException)
      }
  }

  private def auditRequest(transactionName: String,
                           detail: Map[String, String])(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(transactionName = transactionName,
      path = appConfig.customsDataStoreUrl, detail = detail, auditType = "DataStoreRequest")

  private def auditResponse(transactionName: String,
                            response: HttpResponse,
                            url: String)(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(
      transactionName = transactionName,
      path = url,
      detail = Map("status" -> s"${response.status}", "message" -> s"${response.body}"),
      auditType = "DataStoreResponse"
    )
}
