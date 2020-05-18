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

package integration

import integration.stubservices.EmailVerificationStubService
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.customs.emailfrontend.connectors.EmailVerificationConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.{
  EmailAlreadyVerified,
  EmailVerificationRequestFailure,
  EmailVerificationRequestResponse,
  EmailVerificationRequestSent
}
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationStateHttpParser.{
  EmailNotVerified,
  EmailVerificationStateErrorResponse,
  EmailVerified
}
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.http._
import utils.Constants._
import utils.WireMockRunner

class EmailVerificationIntegrationSpec extends IntegrationSpec with WireMockRunner {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.email-verification.host" -> wireMockHost,
        "microservice.services.email-verification.port" -> wireMockPort,
        "auditing.enabled" -> false
      )
    )
    .build()

  private lazy val connector =
    app.injector.instanceOf[EmailVerificationConnector]
  private val emailAddress = "test@example.com"
  private val emailDetails = EmailDetails(None, "test@example.com", None)
  private val expectedContinueUrl = "/customs/test-continue-url"
  private val eoriNumber = "EORINumber"

  implicit val hc: HeaderCarrier = HeaderCarrier()

  before {
    resetMockServer()
  }

  override def beforeAll(): Unit =
    startMockServer()

  override def afterAll(): Unit =
    stopMockServer()

  "Calling getEmailVerificationState" when {

    "the email is verified" should {
      "return an EmailVerified response" in {
        EmailVerificationStubService.stubEmailVerified()
        val expected = Right(EmailVerified)
        val result =
          connector.getEmailVerificationState(emailAddress).futureValue

        result mustBe expected
      }
    }

    "the email is not verified" should {
      "return an EmailNotVerified response" in {
        EmailVerificationStubService.stubEmailNotVerified()
        val expected = Right(EmailNotVerified)
        val result = connector
          .getEmailVerificationState("notverified@gmail.com")
          .futureValue

        result mustBe expected
      }
    }

    "the email service Internal Server Error" should {
      "return an Internal Server Error" in {
        EmailVerificationStubService.stubEmailVerifiedInternalServerError()
        val expected = Left(
          EmailVerificationStateErrorResponse(
            INTERNAL_SERVER_ERROR,
            EmailVerificationStubService.internalServerErrorResponse
          )
        )
        val result =
          connector.getEmailVerificationState(emailAddress).futureValue

        result mustBe expected
      }
    }
  }

  "Calling createEmailVerificationRequest" when {
    "the post is successful" should {
      "return an EmailVerificationRequestSent" in {
        EmailVerificationStubService.stubVerificationRequestSent()
        val expected = Right(EmailVerificationRequestSent)
        val result = connector
          .createEmailVerificationRequest(emailDetails, expectedContinueUrl, eoriNumber)
          .futureValue

        result mustBe expected
      }
    }

    "the email is already verified" should {
      "return an EmailAlreadyVerified" in {
        EmailVerificationStubService.stubEmailAlreadyVerified()
        val expected = Right(EmailAlreadyVerified)
        val result: EmailVerificationRequestResponse = connector
          .createEmailVerificationRequest(emailDetails, expectedContinueUrl, eoriNumber)
          .futureValue

        result mustBe expected
      }
    }

    "the email service Internal Server Error" should {
      "return an Internal Server Error" in {
        EmailVerificationStubService.stubVerificationRequestError()
        val expected = Left(
          EmailVerificationRequestFailure(
            INTERNAL_SERVER_ERROR,
            EmailVerificationStubService.internalServerErrorResponse
          )
        )
        val result: EmailVerificationRequestResponse = connector
          .createEmailVerificationRequest(emailDetails, expectedContinueUrl, eoriNumber)
          .futureValue

        result mustBe expected
      }
    }
  }
}
