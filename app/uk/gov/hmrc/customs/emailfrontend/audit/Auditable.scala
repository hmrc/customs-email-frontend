/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{Audit, DataEvent}

class Auditable @Inject()(auditConnector: AuditConnector, appConfig: AppConfig) {

  private val audit = Audit(appConfig.appName, auditConnector)

  def sendDataEvent(transactionName: String, path: String = "N/A", detail: Map[String, String], auditType: String)(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(DataEvent(
      appConfig.appName,
      auditType,
      tags = hc.toAuditTags(transactionName, path),
      detail = hc.toAuditDetails(detail.toSeq: _*))
    )
}
