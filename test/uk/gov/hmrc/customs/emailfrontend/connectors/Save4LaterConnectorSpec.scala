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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.*
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.{BadRequest, UnhandledException}
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, JourneyType, ReferrerName}
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpReads, HttpResponse, SessionId}

import scala.concurrent.{ExecutionContext, Future}

class Save4LaterConnectorSpec extends SpecBase {

  val mockHttpClient: HttpClientV2             = mock[HttpClientV2]
  val requestBuilder: RequestBuilder           = mock[RequestBuilder]
  val sessionId: SessionId                     = SessionId("session_1234")
  implicit override lazy val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))

  "Save4LaterConnector" should {

    "GET email returns a response with body when OK response received" in new Setup {

      private val emailDetails = EmailDetails(None, "test@test.com", None)

      when(requestBuilder.execute(any[HttpReads[EmailDetails]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailDetails))
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]
        val result    = connector.getEmailDetails("id", "key").futureValue

        result shouldBe Some(emailDetails)
      }
    }

    "GET journey type returns a response with body when OK response received" in new Setup {

      private val journeyType = JourneyType(true)

      when(requestBuilder.execute(any[HttpReads[JourneyType]], any[ExecutionContext]))
        .thenReturn(Future.successful(journeyType))
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getJourneyType("id", "key").futureValue
        result shouldBe Some(journeyType)
      }
    }

    "GET referer returns a response with body when OK response received" in new Setup {

      private val referrerName = ReferrerName("Name", "continueUrl")

      when(requestBuilder.execute(any[HttpReads[ReferrerName]], any[ExecutionContext]))
        .thenReturn(Future.successful(referrerName))
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getReferrerName("id", "key").futureValue
        result shouldBe Some(referrerName)
      }
    }

    "GET referrer returns 'none' when NOT_FOUND response received" in new Setup {

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, emptyString)))
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getReferrerName("id", "key").futureValue
        result shouldBe None
      }
    }

    "GET Journey type returns 'none' when NOT_FOUND response received" in new Setup {

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, emptyString)))
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getJourneyType("id", "key").futureValue
        result shouldBe None
      }
    }

    "GET email details returns 'none' when NOT_FOUND response received" in new Setup {

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, emptyString)))
      when(mockHttpClient.get(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getEmailDetails("id", "key").futureValue
        result shouldBe None
      }
    }

    "DELETE returns unit when NO_CONTENT response received" in new Setup {

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))
      when(mockHttpClient.delete(any)(any)).thenReturn(requestBuilder)

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.delete(emptyString).futureValue
        result shouldBe (())
      }
    }

    "DELETE returns exception when BAD_REQUEST response received" in new Setup {

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, emptyString)))
      when(mockHttpClient.delete(any)(any)).thenReturn(requestBuilder)

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result = connector.delete(emptyString).futureValue
        result shouldBe Left(BadRequest)
      }
    }

    "DELETE returns unhandled exception when BAD_REQUEST exception response received" in new Setup {

      val badRequestException = new BadRequestException("testMessage")
      val connector           = app.injector.instanceOf[Save4LaterConnector]

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(badRequestException))
      when(mockHttpClient.delete(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val result = connector.delete(emptyString).futureValue
        result shouldBe Left(UnhandledException)
      }
    }

    "PUT returns unit when NO_CONTENT response received" in new Setup {

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))
      when(mockHttpClient.put(any)(any)).thenReturn(requestBuilder)

      val testJson  = Json.toJson("test")
      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe (())
      }
    }

    "PUT returns unit when OK response received" in new Setup {

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(OK, emptyString)))
      when(mockHttpClient.put(any)(any)).thenReturn(requestBuilder)

      val testJson  = Json.toJson("test")
      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe (())
      }
    }

    "PUT returns unit when CREATED response received" in new Setup {

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(CREATED, emptyString)))
      when(mockHttpClient.put(any)(any)).thenReturn(requestBuilder)

      val testJson  = Json.toJson("test")
      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe (())
      }
    }

    "PUT returns exception when BAD_REQUEST response received" in new Setup {

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, emptyString)))
      when(mockHttpClient.put(any)(any)).thenReturn(requestBuilder)

      val testJson  = Json.toJson("test")
      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe Left(BadRequest)
      }
    }

    "PUT returns unhandledException when BAD_REQUEST response received" in new Setup {

      val badRequestException = new BadRequestException("testMessage")
      val testJson            = Json.toJson("test")
      val connector           = app.injector.instanceOf[Save4LaterConnector]

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(badRequestException))
      when(mockHttpClient.put(any)(any)).thenReturn(requestBuilder)

      running(app) {
        val result = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe Left(UnhandledException)
      }
    }
  }

  trait Setup {
    protected val app: Application = applicationBuilder()
      .overrides(
        inject.bind[HttpClientV2].toInstance(mockHttpClient),
        inject.bind[RequestBuilder].toInstance(requestBuilder)
      )
      .build()
  }
}
