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
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.SubscriptionDisplayResponse
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionDisplayConnectorSpec extends SpecBase with BeforeAndAfterEach {

  private val mockHttp = mock[HttpClient]
  private val mockAuditable = mock[Auditable]
  private val mockAppConfig = mock[AppConfig]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val url = "customs-email-proxy/subscription-display"
  private val testEori = "GB1234556789"
  val emailVerificationTimeStamp = "2016-3-17T9:30:47.114"
  private val someSubscriptionDisplayResponse = SubscriptionDisplayResponse(
    Some("test@test.com"),
    Some(emailVerificationTimeStamp),
    Some("statusCode"),
    Some("FAIL")
  )
  private val noneSubscriptionDisplayResponse =
    SubscriptionDisplayResponse(None, None, None, None)

  val testConnector =
    new SubscriptionDisplayConnector(mockAppConfig, mockHttp, mockAuditable)

  override def beforeEach(): Unit = {
    reset(mockHttp, mockAuditable, mockAppConfig)
    when(mockAppConfig.subscriptionDisplayUrl).thenReturn(url)
  }

  "SubscriptionDisplayConnector" should {
    "successfully send a query request return SubscriptionDisplayResponse with email inside" in {
      when(
        mockHttp.GET(meq(url),
                     any[Seq[(String, String)]],
                     any[Seq[(String, String)]])(
          any[HttpReads[SubscriptionDisplayResponse]],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      ).thenReturn(Future.successful(someSubscriptionDisplayResponse))
      doNothing
        .when(mockAuditable)
        .sendDataEvent(any, any, any, any)(any[HeaderCarrier])
      testConnector
        .subscriptionDisplay(testEori)
        .futureValue shouldBe someSubscriptionDisplayResponse
    }

    "successfully send a query request return SubscriptionDisplayResponse with none for a value inside" in {
      when(
        mockHttp.GET(meq(url),
                     any[Seq[(String, String)]],
                     any[Seq[(String, String)]])(
          any[HttpReads[SubscriptionDisplayResponse]],
          any[HeaderCarrier],
          any[ExecutionContext]
        )
      ).thenReturn(Future.successful(noneSubscriptionDisplayResponse))
      doNothing
        .when(mockAuditable)
        .sendDataEvent(any, any, any, any)(any[HeaderCarrier])
      testConnector
        .subscriptionDisplay(testEori)
        .futureValue shouldBe noneSubscriptionDisplayResponse
    }
  }
}
