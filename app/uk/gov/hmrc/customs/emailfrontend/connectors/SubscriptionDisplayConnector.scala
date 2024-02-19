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

import java.util.UUID
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.SubscriptionDisplayResponse
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HttpClient, _}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDisplayConnector @Inject()(appConfig: AppConfig, http: HttpClient, auditable: Auditable)
                                            (implicit ec: ExecutionContext) {

  def subscriptionDisplay(eori: String)(implicit hc: HeaderCarrier): Future[SubscriptionDisplayResponse] = {
    val request = ("EORI" -> eori) :: buildQueryParams

    http.GET[SubscriptionDisplayResponse](appConfig.subscriptionDisplayUrl, request).map { displayResponse =>
      auditResponse(
        transactionName = "customs-email-subscription-display",
        auditType = "subscriptionDisplayResponse",
        response = displayResponse,
        url = appConfig.subscriptionDisplayUrl
      )
      displayResponse
    }
  }

  private def auditResponse(transactionName: String,
                            auditType: String,
                            response: SubscriptionDisplayResponse,
                            url: String)
                           (implicit hc: HeaderCarrier): Unit =

    auditable.sendDataEvent(
      transactionName = transactionName,
      path = url,
      detail = Map(
        "emailAddress" -> response.email.getOrElse("No email address received"),
        "emailVerificationTimestamp" -> response.emailVerificationTimestamp
          .getOrElse("No emailVerificationTimestamp  received"),
        "statusText" -> response.statusText.getOrElse("No status text"),
        "paramValue" -> response.paramValue.getOrElse("paramValue")
      ),
      auditType = auditType)

  private def buildQueryParams: List[(String, String)] =
    List("regime" -> "CDS", "acknowledgementReference" -> generateUUIDAsString)

  private def generateUUIDAsString: String = UUID.randomUUID().toString.replace("-", "")
}
