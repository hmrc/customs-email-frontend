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

package integration

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{equalToJson, postRequestedFor, urlEqualTo}
import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.NO_CONTENT
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.domain.DataStoreRequest
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.HeaderCarrier

class CustomsDataStoreIntegrationSpec extends IntegrationSpec with CustomsDataStoreService with ScalaFutures {

  val Port: Int = sys.env.getOrElse("WIREMOCK_SERVICE_LOCATOR_PORT", "11111").toInt
  val Host: String = "localhost"
  val eori = Eori("GB0123456789")
  val Email = "a@b.com"

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.tax-enrolments.host" -> Host,
        "microservice.services.tax-enrolments.port" -> Port,
        "microservice.services.tax-enrolments.context" -> "/tax-enrolments/subscriptions",
        "microservice.services.customs-data-store.host" -> Host,
        "microservice.services.customs-data-store.port" -> Port,
        "microservice.services.customs-data-store.context" -> "/customs-data-store/graphql",
        "microservice.services.subscription-display.host" -> Host,
        "microservice.services.subscription-display.port" -> Port,
        "microservice.services.subscription-display.context" -> "/subscriptions/subscriptiondisplay/v1",
        "auditing.enabled" -> false,
        "auditing.consumer.baseUri.host" -> Host,
        "auditing.consumer.baseUri.port" -> Port
      )
    )
    .build()

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val expectedUrl = "/customs-data-store/graphql"
  private lazy val customsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]

  private val dataStoreRequest = DataStoreRequest(eori.id, Email)
  private val dataStoreRequestQuery = s"""{"query" : "mutation {byEori(eoriHistory:{eori:\\"${eori.id}\\"}, notificationEmail:{address:\\"$Email\\"})}"}"""

  override def beforeAll: Unit = {
    startMockServer()
  }

  override def afterAll: Unit = {
    stopMockServer()
  }

  "CustomsDataStoreConnector" should {
    "call customs data store service with correct url and payload" in {
      await(customsDataStoreConnector.storeEmailAddress(dataStoreRequest))

      WireMock.verify(postRequestedFor(urlEqualTo(expectedUrl)).withRequestBody(equalToJson(dataStoreRequestQuery)))
    }

    "return successful future with correct status when customs data store service returns good status(204)" in {
      returnCustomsDataStoreResponse(expectedUrl, dataStoreRequestQuery, NO_CONTENT)
      await(customsDataStoreConnector.storeEmailAddress(dataStoreRequest)).status mustBe NO_CONTENT
    }
  }
}
