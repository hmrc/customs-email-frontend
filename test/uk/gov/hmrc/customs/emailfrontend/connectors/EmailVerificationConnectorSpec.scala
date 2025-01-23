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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, reset, when}
import play.api.http.Status
import play.api.http.Status.*
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.*
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationStateHttpParser.*
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}

import scala.concurrent.{ExecutionContext, Future}

class EmailVerificationConnectorSpec extends SpecBase {

  private val mockAuditable  = mock[Auditable]
  private val requestBuilder = mock[RequestBuilder]
  private val mockHttpClient = mock[HttpClientV2]

  private val emailBaseUrl = "http://localhost:9744/email-verification"

  val connector = new EmailVerificationConnector(mockHttpClient, mockAppConfig, mockAuditable)

  override def beforeEach(): Unit = {
    reset(mockAuditable, mockAppConfig, mockHttpClient, requestBuilder)

    doNothing.when(mockAuditable).sendDataEvent(any, any, any, any)(any[HeaderCarrier])
    when(mockAppConfig.checkVerifiedEmailUrl).thenReturn(s"$emailBaseUrl/verified-email-check")
    when(mockAppConfig.createEmailVerificationRequestUrl).thenReturn(s"$emailBaseUrl/verification-requests")

    when(mockAppConfig.emailVerificationTemplateId).thenReturn("verifyEmailAddress")
    when(mockAppConfig.emailVerificationLinkExpiryDuration).thenReturn("P3D")
  }

  "Calling getEmailVerificationState" when {
    "the email is verified" should {
      "return an EmailVerified response" in {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[EmailVerificationStateResponse]], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(EmailVerified)))
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

        val result =
          connector.getEmailVerificationState("email-address").futureValue

        result shouldBe Right(EmailVerified)
      }
    }

    "the email is not verified" should {
      "return an EmailNotVerified response" in {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[EmailVerificationStateResponse]], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(EmailNotVerified)))
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

        val result = connector.getEmailVerificationState("email-address").futureValue

        result shouldBe Right(EmailNotVerified)
      }
    }

    "the email service provides an unexpected state" should {
      "return an EmailVerificationStateErrorResponse" in {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[EmailVerificationStateResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(Left(EmailVerificationStateErrorResponse(INTERNAL_SERVER_ERROR, "Internal Server Error")))
          )
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

        val result = connector.getEmailVerificationState("email-address").futureValue

        result shouldBe Left(EmailVerificationStateErrorResponse(INTERNAL_SERVER_ERROR, "Internal Server Error"))
      }
    }
  }

  "Calling createEmailVerificationRequest" when {
    "the request is successful" should {
      "return an EmailVerificationRequestSent" in {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[EmailVerificationRequestResponse]], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(EmailVerificationRequestSent)))
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

        val result = connector
          .createEmailVerificationRequest(
            EmailDetails(Some("old-email-address"), "email-address", None),
            "test-continue-url",
            "EORINumber"
          )
          .futureValue

        result shouldBe Right(EmailVerificationRequestSent)
      }
    }

    "the email is already verified" should {
      "return an EmailAlreadyVerified" in {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[EmailVerificationRequestResponse]], any[ExecutionContext]))
          .thenReturn(Future.successful(Right(EmailAlreadyVerified)))
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

        val result = connector
          .createEmailVerificationRequest(EmailDetails(None, "email-address", None), "test-continue-url", "EORINumber")
          .futureValue

        result shouldBe Right(EmailAlreadyVerified)
      }
    }

    "the request is not successful" should {
      "return an Internal Server Error" in {

        when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[EmailVerificationRequestResponse]], any[ExecutionContext]))
          .thenReturn(
            Future
              .successful(Left(EmailVerificationRequestFailure(Status.INTERNAL_SERVER_ERROR, "Internal server error")))
          )
        when(mockHttpClient.post(any)(any)).thenReturn(requestBuilder)

        val result = connector
          .createEmailVerificationRequest(EmailDetails(None, "email-address", None), "test-continue-url", "EORINumber")
          .futureValue

        result shouldBe Left(EmailVerificationRequestFailure(Status.INTERNAL_SERVER_ERROR, "Internal server error"))
      }
    }
  }
}
