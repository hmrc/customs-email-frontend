/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.http.HeaderNames.CONTENT_TYPE
import play.api.http.MimeTypes
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.logging.CdsLogger
import uk.gov.hmrc.customs.emailfrontend.model.{Eori, UpdateEmail}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomsDataStoreConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient, audit: Auditable)(
  implicit ec: ExecutionContext
) {

  private[connectors] lazy val url: String = appConfig.customsDataStoreUrl

  def storeEmailAddress(eori: Eori, email: String, timestamp: DateTime)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    val request = UpdateEmail(eori, email, timestamp)

    auditRequest("DataStoreEmailRequestSubmitted", Map("eori number" -> eori.id, "emailAddress" -> email, "timestamp" -> timestamp.toString()))

    httpClient
      .doPost[UpdateEmail](url, request, Seq(CONTENT_TYPE -> MimeTypes.JSON))(implicitly, hc, ec)
      .map { response =>
        auditResponse("DataStoreResponseReceived", response, url)
        response
      }.recoverWith {
      case e: Throwable =>
        CdsLogger.error(s"Call to data stored failed url=$url, exception=$e")
        Future.failed(e)
    }
  }

  private def auditRequest(transactionName: String, detail: Map[String, String])(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(transactionName = transactionName, path = url, detail = detail, auditType = "DataStoreRequest")

  private def auditResponse(transactionName: String, response: HttpResponse, url: String)(
    implicit hc: HeaderCarrier
  ): Unit =
    audit.sendDataEvent(
      transactionName = transactionName,
      path = url,
      detail = Map("status" -> s"${response.status}", "message" -> s"${response.body}"),
      auditType = "DataStoreResponse"
    )
}
