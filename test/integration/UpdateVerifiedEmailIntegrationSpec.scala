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

import integration.stubservices.UpdateVerifiedEmailStubService
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.customs.emailfrontend.connectors.UpdateVerifiedEmailConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Constants._
import utils.WireMockRunner

class UpdateVerifiedEmailIntegrationSpec
    extends IntegrationSpec
    with WireMockRunner {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.customs-email-proxy.host" -> wireMockHost,
        "microservice.services.customs-email-proxy.port" -> wireMockPort,
        "auditing.enabled" -> false
      )
    )
    .build()

  private lazy val connector =
    app.injector.instanceOf[UpdateVerifiedEmailConnector]

  private val verifiedEmailResponse =
    UpdateVerifiedEmailStubService.updatedVerifiedEmailResponse
  private val request = Json
    .parse(UpdateVerifiedEmailStubService.verifiedEmailRequest)
    .as[VerifiedEmailRequest]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  before {
    resetMockServer()
  }

  override def beforeAll(): Unit =
    startMockServer()

  override def afterAll(): Unit =
    stopMockServer()

  "Calling updateVerifiedEmail" when {
    "the email was updated successfully" should {
      "return an EmailVerified response" in {
        UpdateVerifiedEmailStubService.stubEmailUpdated(verifiedEmailResponse)
        val expected =
          Right(Json.parse(verifiedEmailResponse).as[VerifiedEmailResponse])
        val result = connector.updateVerifiedEmail(request, None).futureValue

        result mustBe expected
      }
    }

    "service returned 400" should {
      "return BadRequest response" in {
        UpdateVerifiedEmailStubService.stubBadRequest()
        val expected = Left(BadRequest)
        val result = connector.updateVerifiedEmail(request, None).futureValue

        result mustBe expected
      }
    }

    "service returned 403" should {
      "return an EmailVerificationRequestSent" in {
        UpdateVerifiedEmailStubService.stubForbidden()
        val expected = Left(Forbidden)
        val result = connector.updateVerifiedEmail(request, None).futureValue

        result mustBe expected
      }
    }

    "service returned 500" should {
      "return ServiceUnavailable response" in {
        UpdateVerifiedEmailStubService.stubServiceUnavailable()
        val expected = Left(ServiceUnavailable)
        val result = connector.updateVerifiedEmail(request, None).futureValue

        result mustBe expected
      }
    }

    "service returned non fatal" should {
      "return non unhandled response" in {
        UpdateVerifiedEmailStubService.stubEmailUpdatedResponseWithStatus("",
                                                                          502)
        val expected = Left(UnhandledException)
        val result = connector.updateVerifiedEmail(request, None).futureValue

        result mustBe expected
      }
    }
  }
}
