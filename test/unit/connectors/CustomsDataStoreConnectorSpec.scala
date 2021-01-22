/*
 * Copyright 2021 HM Revenue & Customs
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
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class CustomsDataStoreConnectorSpec
    extends PlaySpec
    with ScalaFutures
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockHttp = mock[HttpClient]
  private val mockAuditable = mock[Auditable]
  private val mockAppConfig = mock[AppConfig]
  private implicit val hc = HeaderCarrier()

  val testConnector =
    new CustomsDataStoreConnector(mockAppConfig, mockHttp, mockAuditable)

  val url = "/customs-data-store/graphql"
  val testEori = Eori("GB1234556789")
  val testEmail = "email@test.com"
  val query =
    s"""{"query" : "mutation {byEori(eoriHistory:{eori:\\"${testEori.id}\\"}, notificationEmail:{address:\\"$testEmail\\"})}"}"""
  val token = "secret-token"
  val headers = Seq("Content-Type" -> "application/json")
  val header: HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer $token")))

  override def beforeEach(): Unit = {
    reset(mockHttp, mockAuditable, mockAppConfig)
    when(mockAppConfig.customsDataStoreUrl).thenReturn(url)
    when(mockAppConfig.customsDataStoreToken).thenReturn(token)
  }

  "CustomsDataStoreConnector" should {
    "successfully send a query request to customs data store and return the OK response" in {
      when(
        mockHttp.doPost(meq(url), meq(Json.parse(query)), meq(headers))(
          any(),
          meq(header),
          any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(200)))
      doNothing()
        .when(mockAuditable)
        .sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
      testConnector
        .storeEmailAddress(testEori, testEmail)
        .futureValue
        .status mustBe 200
    }

    "return the failure response from customs data store" in {
      when(
        mockHttp.doPost(meq(url), meq(Json.parse(query)), meq(headers))(
          any(),
          meq(header),
          any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(400)))
      doNothing()
        .when(mockAuditable)
        .sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
      testConnector
        .storeEmailAddress(testEori, testEmail)
        .futureValue
        .status mustBe 400
    }
  }
}
