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

package uk.gov.hmrc.customs.emailfrontend.connectors

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{eq => meq}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{NOT_FOUND, NO_CONTENT}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.{Eori, UpdateEmail}
import uk.gov.hmrc.customs.emailfrontend.services.DateTimeService
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnectorSpec extends SpecBase with BeforeAndAfterEach {

  private val mockHttp = mock[HttpClient]
  private val mockAuditable = mock[Auditable]
  private val mockAppConfig = mock[AppConfig]
  private val mockDateTimeService = mock[DateTimeService]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val testConnector = new CustomsDataStoreConnector(mockAppConfig, mockHttp, mockAuditable)

  private val url = "/customs-data-store/update-email"
  private val testEori: Eori = Eori("GB1234556789")
  private val testEmail = "email@test.com"
  private val testDateTime = new DateTime("2021-01-01T11:11:11.111Z")
  private val requestBody: UpdateEmail = UpdateEmail(testEori, testEmail, testDateTime)
  private val headers = Seq("Content-Type" -> "application/json")

  override def beforeEach(): Unit = {
    reset(mockHttp, mockAuditable, mockAppConfig)
    when(mockAppConfig.customsDataStoreUrl).thenReturn(url)
    when(mockDateTimeService.nowUtc())
      .thenReturn(testDateTime)
  }

  "CustomsDataStoreConnector" should {
    "successfully send a query request to customs data store and return the OK response" in {
      when(
        mockHttp.POST[UpdateEmail, HttpResponse](
          meq(url),
          meq(requestBody),
          meq(headers))(any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, "")))
      doNothing
        .when(mockAuditable)
        .sendDataEvent(any, any, any, any)(any[HeaderCarrier])
      testConnector
        .storeEmailAddress(testEori, testEmail, testDateTime)
        .futureValue
        .status shouldBe NO_CONTENT
    }

    "return NOT_FOUND response from customs data store" in {
      when(
        mockHttp.POST[UpdateEmail, HttpResponse](
          meq(url),
          meq(requestBody),
          meq(headers))(any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, "")))
      doNothing
        .when(mockAuditable)
        .sendDataEvent(any, any, any, any)(any[HeaderCarrier])
      testConnector
        .storeEmailAddress(testEori, testEmail, testDateTime)
        .futureValue
        .status shouldBe NOT_FOUND
    }

    "return the BAD_REQUEST exception response from customs data store" in {

      val badRequestException = new BadRequestException("testMessage")

      when(mockHttp.POST[UpdateEmail, HttpResponse](meq(url), meq(requestBody), meq(headers))
        (any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.failed(badRequestException))

      doNothing.when(mockAuditable).sendDataEvent(any, any, any, any)(any[HeaderCarrier])

      assertThrows[BadRequestException](await(testConnector.storeEmailAddress(testEori, testEmail, testDateTime)))

    }

    "UpdateEmail model object serializes correctly" in {
      val updateEmail = UpdateEmail(testEori, testEmail, testDateTime)
      Json
        .toJson(updateEmail)
        .toString() shouldBe """{"eori":"GB1234556789","address":"email@test.com","timestamp":"2021-01-01T11:11:11Z"}"""
    }
  }
}
