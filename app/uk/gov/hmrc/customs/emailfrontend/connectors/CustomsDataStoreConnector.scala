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

package uk.gov.hmrc.customs.emailfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CustomsDataStoreConnector @Inject()(appConfig: AppConfig, httpClient: HttpClient, audit: Auditable) {

  private val url: String = appConfig.customsDataStoreUrl

  def storeEmailAddress(eori: Eori, email: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val query = s"""{"query" : "mutation {byEori(eoriHistory:{eori:\\"${eori.id}\\"}, notificationEmail:{address:\\"$email\\"})}"}"""
    val header: HeaderCarrier = hc.copy(authorization = Some(Authorization(s"Bearer ${appConfig.customsDataStoreToken}")))

    val detail = Map("eori number" -> eori.id, "emailAddress" -> email)
    auditRequest("DataStoreEmailRequestSubmitted", detail)

    httpClient.doPost[JsValue](url, Json.parse(query), Seq("Content-Type" -> "application/json"))(implicitly, header)
      .map { response =>
        auditResponse("DataStoreResponseReceived", response, url)
        response
      }
  }

  private def auditRequest(transactionName: String, detail: Map[String, String])(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(
      transactionName = transactionName,
      path = url,
      detail = detail,
      auditType = "DataStoreRequest"
    )

  private def auditResponse(transactionName: String, response: HttpResponse, url: String)(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(
      transactionName = transactionName,
      path = url,
      detail = Map("status" -> s"${response.status}", "message" -> s"${response.body}"),
      auditType = "DataStoreResponse"
    )
}