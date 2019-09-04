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
import play.api.test.Helpers.NO_CONTENT
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.HeaderCarrier

class CustomsDataStoreIntegrationSpec extends IntegrationSpec with CustomsDataStoreService with ScalaFutures {

  val eori = Eori("GB0123456789")
  val Email = "a@b.com"

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val expectedUrl = "/customs-data-store/graphql"
  private lazy val customsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]

  private val dataStoreRequestQuery = s"""{"query" : "mutation {byEori(eoriHistory:{eori:\\"${eori.id}\\"}, notificationEmail:{address:\\"$Email\\"})}"}"""

  override def beforeAll: Unit = {
    startMockServer()
  }

  override def afterAll: Unit = {
    stopMockServer()
  }

  "CustomsDataStoreConnector" should {
    "call customs data store service with correct url and payload" in {
      customsDataStoreConnector.storeEmailAddress(eori, Email).futureValue

      WireMock.verify(postRequestedFor(urlEqualTo(expectedUrl)).withRequestBody(equalToJson(dataStoreRequestQuery)))
    }

    "return successful future with correct status when customs data store service returns good status(204)" in {
      returnCustomsDataStoreResponse(expectedUrl, dataStoreRequestQuery, NO_CONTENT)
      customsDataStoreConnector.storeEmailAddress(eori, Email).futureValue.status mustBe NO_CONTENT
    }
  }
}
