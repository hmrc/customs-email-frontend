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

import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}
import uk.gov.hmrc.play.audit.AuditExtensions.AuditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.model.{Audit, DataEvent}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, when}
import org.scalatest.matchers.must.Matchers.mustBe

import scala.concurrent.{ExecutionContext, Future}

class AuditableSpec extends SpecBase {

  "sendDataEvent" should {
    "send a data event" ignore new Setup {

      doNothing.when(mockAudit).sendDataEvent(any)(any)
      when(mockConnector.sendEvent(dataEvent)(hc, ec)).thenReturn(Future.successful(Success))

      auditableOb.sendDataEvent("test_transaction", "test_path", Map("test_other" -> "other"), "test_audit")(
        hc
      ) mustBe ()
    }
  }

  trait Setup {
    implicit val hc: HeaderCarrier =
      HeaderCarrier(requestId = Some(RequestId("test_value")), sessionId = Some(uk.gov.hmrc.http.SessionId("test_id")))

    implicit val ec: ExecutionContext                   = scala.concurrent.ExecutionContext.Implicits.global
    implicit val auditHeaderCarrier: AuditHeaderCarrier = new AuditHeaderCarrier(hc)

    val dataEvent: DataEvent          = DataEvent("test_source", "test", "test")
    val mockConnector: AuditConnector = mock[AuditConnector]
    val mockAudit: Audit              = mock[Audit]

    val app: Application = applicationBuilder[FakeIdentifierAgentAction]()
      .overrides(
        inject.bind[AuditConnector].toInstance(mockConnector),
        inject.bind[Audit].toInstance(mockAudit)
      )
      .build()

    val auditableOb: Auditable = app.injector.instanceOf[Auditable]
  }
}
