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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, urlEqualTo}
import com.typesafe.config.ConfigFactory
import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.http.Status.{BAD_REQUEST, CREATED, OK, SERVICE_UNAVAILABLE}
import play.api.test.Helpers.await
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.SendEmailRequest
import uk.gov.hmrc.customs.emailfrontend.utils.WireMockSupportProvider
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import com.github.tomakehurst.wiremock.http.Fault.CONNECTION_RESET_BY_PEER
import com.github.tomakehurst.wiremock.http.RequestMethod.POST

import scala.concurrent.ExecutionContext
import play.api.test.Helpers.defaultAwaitTimeout

class EmailConnectorSpec extends AnyWordSpecLike with Matchers with MockitoSugar with WireMockSupportProvider {

  "sendEmail" should {

    "return 201 response when email is sent successfully" in new Setup {

      val sendEmailRequest: SendEmailRequest =
        SendEmailRequest(Seq("test@test.com"), "test_template", Map("emailAddress" -> "test@test.com"))

      val emailRequestJsString: String = Json.toJson(sendEmailRequest).toString

      wireMockServer.stubFor(
        post(urlEqualTo(emailServicePath))
          .withRequestBody(equalToJson(emailRequestJsString))
          .willReturn(
            aResponse()
              .withStatus(CREATED)
          )
      )

      val result: HttpResponse =
        await(connector.sendEmail("test@test.com", "test_template", Map("emailAddress" -> "test@test.com")))

      result.status mustBe CREATED

      verifyEndPointUrlHit(emailServicePath, POST)
    }

    "return 200 response when email is sent successfully" in new Setup {

      val sendEmailRequest: SendEmailRequest =
        SendEmailRequest(Seq("test@test.com"), "test_template", Map("emailAddress" -> "test@test.com"))

      val emailRequestJsString: String = Json.toJson(sendEmailRequest).toString

      wireMockServer.stubFor(
        post(urlEqualTo(emailServicePath))
          .withRequestBody(equalToJson(emailRequestJsString))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      val result: HttpResponse =
        await(connector.sendEmail("test@test.com", "test_template", Map("emailAddress" -> "test@test.com")))

      result.status mustBe OK

      verifyEndPointUrlHit(emailServicePath, POST)
    }

    "return HttpResponse with correct error code when error occurs while sending email" in new Setup {
      val sendEmailRequest: SendEmailRequest =
        SendEmailRequest(Seq("test@test.com"), "test_template", Map("emailAddress" -> "test@test.com"))

      val emailRequestJsString: String = Json.toJson(sendEmailRequest).toString

      wireMockServer.stubFor(
        post(urlEqualTo(emailServicePath))
          .withRequestBody(equalToJson(emailRequestJsString))
          .willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
          )
      )

      val result: HttpResponse =
        await(connector.sendEmail("test@test.com", "test_template", Map("emailAddress" -> "test@test.com")))

      result.status mustBe BAD_REQUEST

      verifyEndPointUrlHit(emailServicePath, POST)
    }

    "return SERVICE_UNAVAILABLE when connection gets reset while sending email" in new Setup {
      val sendEmailRequest: SendEmailRequest =
        SendEmailRequest(Seq("test@test.com"), "test_template", Map("emailAddress" -> "test@test.com"))

      val emailRequestJsString: String = Json.toJson(sendEmailRequest).toString

      wireMockServer.stubFor(
        post(urlEqualTo(emailServicePath))
          .withRequestBody(equalToJson(emailRequestJsString))
          .willReturn(
            aResponse()
              .withFault(CONNECTION_RESET_BY_PEER)
          )
      )

      val result: HttpResponse =
        await(connector.sendEmail("test@test.com", "test_template", Map("emailAddress" -> "test@test.com")))

      result.status mustBe SERVICE_UNAVAILABLE

      verifyEndPointUrlHit(emailServicePath, POST)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |    email {
         |       host = $wireMockHost
         |       port = $wireMockPort
         |    }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    lazy implicit val hc: HeaderCarrier    = HeaderCarrier()
    lazy implicit val ec: ExecutionContext = ExecutionContext.global

    val mockConfig: AppConfig = mock[AppConfig]
    val emailServicePath      = "/hmrc/email"

    val app: Application = new GuiceApplicationBuilder()
      .configure(
        "play.filters.csp.nonce.enabled"        -> false,
        "auditing.enabled"                      -> "false",
        "microservice.metrics.graphite.enabled" -> "false",
        "metrics.enabled"                       -> "false"
      )
      .build()

    val httpClient: HttpClientV2  = app.injector.instanceOf[HttpClientV2]
    val connector: EmailConnector = new EmailConnector(mockConfig, httpClient)

    when(mockConfig.emailServiceUrl).thenReturn(s"http://localhost:$wireMockPort$emailServicePath")
  }
}
