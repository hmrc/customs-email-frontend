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

import org.mockito.Mockito.{ when, reset}
import play.api.http.Status._
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.running
import play.api.{Application, inject}
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.{BadRequest, UnhandledException}
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, JourneyType, ReferrerName}
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpClient, HttpResponse, SessionId}
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.http.client.HttpClientV2
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

class Save4LaterConnectorSpec extends SpecBase {

  val mockHttpClient = mock[HttpClientV2]
  val sessionId = SessionId("session_1234")
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(sessionId))

  "Save4LaterConnector" should {

    "GET email returns a response with body when OK response received" in new Setup {

      private val emailDetails = EmailDetails(None, "test@test.com", None)

      when(mockHttpClient.GET[EmailDetails](any, any, any)(any, any, any))
        .thenReturn(Future.successful(emailDetails))

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getEmailDetails("id", "key").futureValue
        result shouldBe Some(emailDetails)
      }
    }

    "GET journey type returns a response with body when OK response received" in new Setup {

      private val journeyType = JourneyType(true)

      when(mockHttpClient.GET[JourneyType](any, any, any)(any, any, any))
        .thenReturn(Future.successful(journeyType))

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getJourneyType("id", "key").futureValue
        result shouldBe Some(journeyType)
      }
    }

    "GET referer returns a response with body when OK response received" in new Setup {

      private val referrerName = ReferrerName("Name", "continueUrl")

      when(mockHttpClient.GET[ReferrerName](any, any, any)(any, any, any))
        .thenReturn(Future.successful(referrerName))

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getReferrerName("id", "key").futureValue
        result shouldBe Some(referrerName)
      }
    }

    "GET referrer returns 'none' when NOT_FOUND response received" in new Setup {

      when(mockHttpClient.GET[HttpResponse](any, any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, emptyString)))

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getReferrerName("id", "key").futureValue
        result shouldBe None
      }
    }

    "GET Journey type returns 'none' when NOT_FOUND response received" in new Setup {

      when(mockHttpClient.GET[HttpResponse](any, any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, emptyString)))

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getJourneyType("id", "key").futureValue
        result shouldBe None
      }
    }

    "GET email details returns 'none' when NOT_FOUND response received" in new Setup {

      when(mockHttpClient.GET[HttpResponse](any, any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(NOT_FOUND, emptyString)))

      running(app) {
        val connector = app.injector.instanceOf[Save4LaterConnector]

        val result = connector.getEmailDetails("id", "key").futureValue
        result shouldBe None
      }
    }

    "DELETE returns unit when NO_CONTENT response received" in new Setup {

      when(mockHttpClient.DELETE[HttpResponse](any, any)(any, any, any)
      ).thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.delete(emptyString).futureValue
        result shouldBe (())
      }
    }

    "DELETE returns exception when BAD_REQUEST response received" in new Setup {

      when(mockHttpClient.DELETE[HttpResponse](any, any)(any, any, any)
      ).thenReturn(Future.successful(HttpResponse(BAD_REQUEST, emptyString)))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result = connector.delete(emptyString).futureValue
        result shouldBe Left(BadRequest)
      }
    }

    "DELETE returns unhandled exception when BAD_REQUEST exception response received" in new Setup {

      val badRequestException = new BadRequestException("testMessage")
      when(mockHttpClient.DELETE[HttpResponse](any, any)(any, any, any)
      ).thenReturn(Future.failed(badRequestException))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result = connector.delete(emptyString).futureValue
        result shouldBe Left(UnhandledException)
      }
    }

    "PUT returns unit when NO_CONTENT response received" in new Setup {

      val testJson = Json.toJson("test")

      when(mockHttpClient.PUT[JsValue, HttpResponse](any, any, any)(any, any, any, any)
      ).thenReturn(Future.successful(HttpResponse(NO_CONTENT, emptyString)))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe (())
      }
    }

    "PUT returns unit when OK response received" in new Setup {

      val testJson = Json.toJson("test")

      when(mockHttpClient.PUT[JsValue, HttpResponse](any, any, any)(any, any, any, any)
      ).thenReturn(Future.successful(HttpResponse(OK, emptyString)))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe (())
      }
    }

    "PUT returns unit when CREATED response received" in new Setup {

      val testJson = Json.toJson("test")

      when(mockHttpClient.PUT[JsValue, HttpResponse](any, any, any)(any, any, any, any)
      ).thenReturn(Future.successful(HttpResponse(CREATED, emptyString)))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result: Unit = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe (())
      }
    }

    "PUT returns exception when BAD_REQUEST response received" in new Setup {

      val testJson = Json.toJson("test")

      when(mockHttpClient.PUT[JsValue, HttpResponse](any, any, any)(any, any, any, any)
      ).thenReturn(Future.successful(HttpResponse(BAD_REQUEST, emptyString)))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe Left(BadRequest)
      }
    }

    "PUT returns unhandledException when BAD_REQUEST response received" in new Setup {

      val badRequestException = new BadRequestException("testMessage")
      val testJson = Json.toJson("test")

      when(mockHttpClient.PUT[JsValue, HttpResponse](any, any, any)(any, any, any, any)
      ).thenReturn(Future.failed(badRequestException))

      val connector = app.injector.instanceOf[Save4LaterConnector]

      running(app) {
        val result = connector.put(emptyString, emptyString, testJson).futureValue
        result shouldBe Left(UnhandledException)
      }
    }
  }

  trait Setup {
    protected val app: Application = applicationBuilder[FakeIdentifierAgentAction]()
      .overrides(inject.bind[HttpClient].toInstance(mockHttpClient)).build()
  }
}
