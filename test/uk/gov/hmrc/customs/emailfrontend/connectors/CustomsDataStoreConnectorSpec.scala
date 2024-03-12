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

import org.mockito.ArgumentMatchers.{eq => meq}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{NOT_FOUND, NO_CONTENT}
import play.api.libs.json.Json
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses._
import uk.gov.hmrc.customs.emailfrontend.model.{Eori, UpdateEmail}
import uk.gov.hmrc.customs.emailfrontend.services.DateTimeService
import uk.gov.hmrc.customs.emailfrontend.utils.CommonUtils.dateFormatter02
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpResponse, InternalServerException}

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnectorSpec extends SpecBase with BeforeAndAfterEach {

  private val mockHttp = mock[HttpClient]
  private val mockAuditable = mock[Auditable]
  private val mockAppConfig = mock[AppConfig]
  private val mockDateTimeService = mock[DateTimeService]
  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val connector = new CustomsDataStoreConnector(mockAppConfig, mockHttp, mockAuditable)
  private val url = "/customs-data-store/update-email"
  private val testEori: Eori = Eori("GB1234556789")
  private val testEmail = "email@test.com"
  private val testDateTime = LocalDateTime.parse("2021-01-01T11:11:11.111Z", dateFormatter02)
  private val requestBody: UpdateEmail = UpdateEmail(testEori, testEmail, testDateTime)
  private val headers = Seq("Content-Type" -> "application/json")

  override def beforeEach(): Unit = {
    reset(mockHttp, mockAuditable, mockAppConfig)
    when(mockAppConfig.customsDataStoreUrl).thenReturn(url)
    when(mockDateTimeService.nowUtc())
      .thenReturn(testDateTime)
  }

  "CustomsDataStoreConnector" should {
    "successfully send a query request to customs data store and return the NO_CONTENT response" in {

      when(mockHttp.POST[UpdateEmail, HttpResponse](any, any, any)(any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))

      doNothing.when(mockAuditable).sendDataEvent(any, any, any, any)(any[HeaderCarrier])

      val result = connector.storeEmailAddress(testEori, testEmail, testDateTime).futureValue
      result.toOption.get.status shouldBe NO_CONTENT
    }

    "return BadRequest response when POST returns NOT_FOUND from customs data store " in {

      when(mockHttp.POST[UpdateEmail, HttpResponse](
        meq(url), meq(requestBody), meq(headers))(any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, emptyString)))

      doNothing.when(mockAuditable).sendDataEvent(any, any, any, any)(any[HeaderCarrier])

      val result = connector.storeEmailAddress(testEori, testEmail, testDateTime).futureValue
      result.swap.getOrElse(BadRequest) shouldBe BadRequest
    }

    "return the BAD_REQUEST exception response from customs data store" in {

      val badRequestException = new BadRequestException("testMessage")

      when(mockHttp.POST[UpdateEmail, HttpResponse](meq(url), meq(requestBody), meq(headers))
        (any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.failed(badRequestException))

      doNothing.when(mockAuditable).sendDataEvent(any, any, any, any)(any[HeaderCarrier])

      val result = connector.storeEmailAddress(testEori, testEmail, testDateTime).futureValue
      result.swap.getOrElse(BadRequest) shouldBe BadRequest
    }

    "return the service_unavailable exception response from customs data store" in {

      val internalServerException = new InternalServerException("testMessage")

      when(mockHttp.POST[UpdateEmail, HttpResponse](meq(url), meq(requestBody), meq(headers))
        (any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.failed(internalServerException))

      doNothing.when(mockAuditable).sendDataEvent(any, any, any, any)(any[HeaderCarrier])

      val result = connector.storeEmailAddress(testEori, testEmail, testDateTime).futureValue
      result.swap.getOrElse(ServiceUnavailable) shouldBe ServiceUnavailable
    }

    "return a non fatal exception response from customs data store" in {

      val interruptedException = new InterruptedException("testMessage")

      when(mockHttp.POST[UpdateEmail, HttpResponse](meq(url), meq(requestBody), meq(headers))
        (any, any, meq(hc), any[ExecutionContext]))
        .thenReturn(Future.failed(interruptedException))

      doNothing.when(mockAuditable).sendDataEvent(any, any, any, any)(any[HeaderCarrier])

      val result = connector.storeEmailAddress(testEori, testEmail, testDateTime).futureValue
      result.swap.getOrElse(UnhandledException) shouldBe UnhandledException
    }

    "UpdateEmail model object serializes correctly" in {
      val updateEmail = UpdateEmail(testEori, testEmail, testDateTime)
      val result = Json.toJson(updateEmail).toString()

      result shouldBe """{"eori":"GB1234556789","address":"email@test.com","timestamp":"2021-01-01T11:11:11Z"}"""
    }
  }
}
