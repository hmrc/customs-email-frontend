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

package unit.connectors


import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{doNothing, reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.model.{Eori, SubscriptionDisplayResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionDisplayConnectorSpec extends PlaySpec with ScalaFutures with MockitoSugar with BeforeAndAfterEach {

  private val mockHttp = mock[HttpClient]
  private val mockAuditable = mock[Auditable]
  private val mockAppConfig = mock[AppConfig]
  private implicit val hc = HeaderCarrier()

  private val url = "customs-hods-proxy/subscription-display"
  private val testEori = Eori("GB1234556789")
  private val someSubscriptionDisplayRespone = SubscriptionDisplayResponse(Some("test@test.com"))
  private val noneSubscriptionDisplayRespone = SubscriptionDisplayResponse(None)

  val testConnector = new SubscriptionDisplayConnector(mockAppConfig, mockHttp, mockAuditable)

  override def beforeEach(): Unit = {
    reset(mockHttp, mockAuditable, mockAppConfig)
    when(mockAppConfig.subscriptionDisplayUrl).thenReturn(url)
  }

  "SubscriptionDisplayConnector" should {
    "successfully send a query request return SubscriptionDisplayResponse with email inside" in {
      when(mockHttp.GET(meq(url), any[Seq[(String, String)]])(any[HttpReads[SubscriptionDisplayResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(someSubscriptionDisplayRespone))
      doNothing().when(mockAuditable).sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
      testConnector.subscriptionDisplay(testEori).futureValue mustBe someSubscriptionDisplayRespone
    }

    "successfully send a query request return SubscriptionDisplayResponse with none for a value inside" in {
      when(mockHttp.GET(meq(url), any[Seq[(String, String)]])(any[HttpReads[SubscriptionDisplayResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(noneSubscriptionDisplayRespone))
      doNothing().when(mockAuditable).sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
      testConnector.subscriptionDisplay(testEori).futureValue mustBe noneSubscriptionDisplayRespone
    }
  }
}
