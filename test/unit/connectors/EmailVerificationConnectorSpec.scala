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

package unit.connectors

import org.mockito.ArgumentMatchers.{eq => meq, _}
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.EmailVerificationConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationStateHttpParser.{EmailNotVerified, EmailVerificationStateErrorResponse, EmailVerificationStateResponse, EmailVerified}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailVerificationConnectorSpec extends PlaySpec
  with ScalaFutures
  with MockitoSugar
  with BeforeAndAfter {

  private val mockAuditable = mock[Auditable]
  private val mockAppConfig = mock[AppConfig]
  private val mockHttpClient = mock[HttpClient]

  val connector = new EmailVerificationConnector(mockHttpClient, mockAppConfig, mockAuditable)
  implicit val hc: HeaderCarrier = HeaderCarrier()

  before {
    reset(mockAuditable, mockAppConfig, mockHttpClient)
    doNothing().when(mockAuditable).sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
    when(mockAppConfig.emailVerificationWithContext).thenReturn("testUrl")
  }

  "Calling getEmailVerificationState" when {
    "the email is verified" should {
      "return an EmailVerified response" in {
        when(mockHttpClient.POST[JsObject, EmailVerificationStateResponse](
          meq("testUrl/verified-email-check"),
          meq(Json.obj("email" -> "emailaddress")), any())(any(), any(), any[HeaderCarrier], any()))
          .thenReturn(Future.successful(Right(EmailVerified)))

        val result = connector.getEmailVerificationState("emailaddress").futureValue

        result mustBe Right(EmailVerified)
      }
    }

    "the email is not verified" should {
      "return an EmailNotVerified response" in {
        when(mockHttpClient.POST[JsObject, EmailVerificationStateResponse](any(), any(), any())(any(), any(), any[HeaderCarrier], any()))
          .thenReturn(Future.successful(Right(EmailNotVerified)))

        val result = connector.getEmailVerificationState("emailaddress").futureValue

        result mustBe Right(EmailNotVerified)
      }
    }

    "the email service provides an unexpected state" should {
      "return an EmailVerificationStateErrorResponse" in {
        when(mockHttpClient.POST[JsObject, EmailVerificationStateResponse](any(), any(), any())(any(), any(), any[HeaderCarrier], any()))
          .thenReturn(Future.successful(Left(EmailVerificationStateErrorResponse(500, "Internal Server Error"))))

        val result = connector.getEmailVerificationState("emailaddress").futureValue

        result mustBe Left(EmailVerificationStateErrorResponse(500, "Internal Server Error"))
      }
    }
  }
}
