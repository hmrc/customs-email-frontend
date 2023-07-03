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

package uk.gov.hmrc.customs.emailfrontend.audit

import javax.inject.Inject
import play.api.libs.json.JsValue
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{Audit, DataEvent, ExtendedDataEvent}

import scala.concurrent.ExecutionContext

class Auditable @Inject()(auditConnector: AuditConnector, appConfig: AppConfig)(implicit ec: ExecutionContext) {

  private val audit = Audit(appConfig.appName, auditConnector)
  private val auditSource: String = appConfig.appName

  def sendDataEvent(transactionName: String, path: String = "N/A", detail: Map[String, String], auditType: String)(
    implicit hc: HeaderCarrier
  ): Unit =
    audit.sendDataEvent(
      DataEvent(
        auditSource,
        auditType,
        tags = hc.toAuditTags(transactionName, path),
        detail = hc.toAuditDetails(detail.toSeq: _*)
      )
    )

  def sendExtendedDataEvent(
    transactionName: String,
    path: String = "N/A",
    tags: Map[String, String] = Map.empty,
    details: JsValue,
    eventType: String
  )(implicit hc: HeaderCarrier): Unit = {

    auditConnector.sendExtendedEvent(
      ExtendedDataEvent(auditSource, eventType, tags = hc.toAuditTags(transactionName, path) ++ tags, detail = details)
    )
  }
}
