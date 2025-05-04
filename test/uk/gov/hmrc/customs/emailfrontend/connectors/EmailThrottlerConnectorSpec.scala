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

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.http.RequestMethod.POST
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.*
import play.api.{Application, Configuration}
import uk.gov.hmrc.customs.emailfrontend.model.EmailRequest
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.customs.emailfrontend.utils.WireMockSupportProvider
import uk.gov.hmrc.http.HeaderCarrier

class EmailThrottlerConnectorSpec extends AnyWordSpecLike with Matchers with MockitoSugar with WireMockSupportProvider {

  "sendEmail" should {
    "return true when the api responds with 202" in new Setup {
      wireMockServer.stubFor(
        post(urlPathMatching(sendEmailEndpointUrl))
          .withRequestBody(equalToJson(Json.toJson(request).toString))
          .willReturn(aResponse().withStatus(ACCEPTED).withBody(emptyString))
      )

      val result: Boolean = await(connector.sendEmail(request))
      result shouldBe true

      verifyExactlyOneEndPointUrlHit(sendEmailEndpointUrl, POST)
    }

    "return false when the api responds with a successful response that isn't 202" in new Setup {

      wireMockServer.stubFor(
        post(urlPathMatching(sendEmailEndpointUrl))
          .withRequestBody(equalToJson(Json.toJson(request).toString))
          .willReturn(ok(emptyString))
      )

      val result: Boolean = await(connector.sendEmail(request))
      result shouldBe false

      verifyExactlyOneEndPointUrlHit(sendEmailEndpointUrl, POST)
    }

    "return false when the api fails due to connection reset" in new Setup {
      wireMockServer.stubFor(
        post(urlPathMatching(sendEmailEndpointUrl))
          .withRequestBody(equalToJson(Json.toJson(request).toString))
          .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
      )

      val result: Boolean = await(connector.sendEmail(request))
      result shouldBe false

      verifyEndPointUrlHit(sendEmailEndpointUrl, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |  customs-financials-email-throttler {
         |            host = $wireMockHost
         |            port = $wireMockPort
         |        }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val sendEmailEndpointUrl       = "/customs-financials-email-throttler/enqueue-email"

    val request: EmailRequest = EmailRequest(List.empty, emptyString, Map.empty, force = true, None, Some("eori"), None)

    val app: Application = new GuiceApplicationBuilder()
      .configure(config)
      .configure(
        "play.filters.csp.nonce.enabled"        -> false,
        "auditing.enabled"                      -> "false",
        "microservice.metrics.graphite.enabled" -> "false",
        "metrics.enabled"                       -> "false"
      )
      .build()

    val connector: EmailThrottlerConnector = app.injector.instanceOf[EmailThrottlerConnector]
  }
}
