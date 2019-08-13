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

package uk.gov.hmrc.customs.emailfrontend.unit.connectors

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.customs.emailfrontend.connectors.EmailVerificationConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.{EmailAlreadyVerified, EmailVerificationRequestFailure, EmailVerificationRequestResponse, EmailVerificationRequestSent}
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationStateHttpParser.{EmailNotVerified, EmailVerificationStateErrorResponse, EmailVerificationStateResponse, EmailVerified}
import uk.gov.hmrc.customs.emailfrontend.stubservices.EmailVerificationStubService
import uk.gov.hmrc.customs.emailfrontend.utils.{IntegrationSpec, WireMockRunner}
import uk.gov.hmrc.http._

class EmailVerificationConnectorSpec extends  IntegrationSpec with BeforeAndAfter with BeforeAndAfterAll with GuiceOneAppPerSuite   with WireMockRunner{

  override implicit lazy val app: Application = new GuiceApplicationBuilder().configure(Map(
    "microservice.services.email-verification.host" -> host,
    "microservice.services.email-verification.port" -> port,
    "microservice.services.email-verification.context" -> "email-verification",
    "microservice.services.email-verification.templateId" -> "verifyEmailAddresssbt",
    "microservice.services.email-verification.LinkExpiryDuration" -> "P1D",
    "auditing.enabled" -> false,
    "auditing.consumer.baseUri.host" -> host,
    "auditing.consumer.baseUri.port" -> host
  )).build()

  private lazy val connector = app.injector.instanceOf[EmailVerificationConnector]
  private val email = "john.doe@example.com"
  private val expectedContinueUrl = "/customs/test-email-continue/"

  implicit val hc: HeaderCarrier = HeaderCarrier()



  val emailVerifiedResponseJson: JsValue = Json.parse("""{"email": "john.doe@example.com"}""")

  val emailVerificationNotFoundJson: JsValue = Json.parse(
    """{
      |  "code": "NOT_VERIFIED",
      |  "message":"Email not verified."
      |}""".stripMargin
  )

  val internalServerErrorJson: JsValue = Json.parse(
    """{
      |  "code": "UNEXPECTED_ERROR",
      |  "message":"An unexpected error occurred."
      |}""".stripMargin
  )

  def before(): Unit = {
    resetMockServer()
  }

  override def beforeAll(): Unit = {
    startMockServer()
  }

  override def afterAll(): Unit = {
    stopMockServer()
  }

  "Calling getEmailVerificationState" when {

    "the email is verified" should {
      "return an EmailVerified response" in {
        EmailVerificationStubService.stubEmailVerified

        val expected = Right(EmailVerified)
        val result: EmailVerificationStateResponse = connector.getEmailVerificationState(email).futureValue

        result mustBe expected
      }
    }
    "the email is not verified" should {

      "return an EmailNotVerified response" in {
        EmailVerificationStubService.stubEmailNotVerified

        val expected = Right(EmailNotVerified)
        val result: EmailVerificationStateResponse = connector.getEmailVerificationState("notverified@gmail.com").futureValue

        result mustBe expected
      }
    }


    "the email service Internal Server Error" should {

      "return an Internal Server Error" in {
        EmailVerificationStubService.stubEmailVerifiedInternalServerError
        val expected = Left(EmailVerificationStateErrorResponse(
          INTERNAL_SERVER_ERROR,
          EmailVerificationStubService.internalServerErrorJson.toString
        ))
        val result: EmailVerificationStateResponse = connector.getEmailVerificationState(email).futureValue

        result mustBe expected
      }
    }

  }


  "Calling createEmailVerificationRequest" when {

    "the post is successful" should {
      "return an EmailVerificationRequestSent" in {
        val expected = Right(EmailVerificationRequestSent)
        EmailVerificationStubService.stubVerificationRequestSent
        val result: EmailVerificationRequestResponse = connector.createEmailVerificationRequest(email,expectedContinueUrl).futureValue

        result mustBe expected
      }
    }

    "the email is already verified" should {

      "return an EmailAlreadyVerified" in {
        val expected = Right(EmailAlreadyVerified)
        EmailVerificationStubService.stubEmailAlreadyVerified


        val result: EmailVerificationRequestResponse = connector.createEmailVerificationRequest(email, expectedContinueUrl).futureValue

        result mustBe expected
      }
    }


    "the email service Internal Server Error" should {

      "return an Internal Server Error" in {

        val expected = Left(EmailVerificationRequestFailure(
          INTERNAL_SERVER_ERROR,
          EmailVerificationStubService.internalServerErrorJson.toString
        ))
        EmailVerificationStubService.stubVerificationRequestError

        val result: EmailVerificationRequestResponse =
          connector.createEmailVerificationRequest("scala@gmail.com", "/home").futureValue

        result mustBe expected

      }
    }

  }



}