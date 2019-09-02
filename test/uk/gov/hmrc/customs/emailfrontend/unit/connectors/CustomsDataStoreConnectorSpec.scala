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

package uk.gov.hmrc.customs.emailfrontend.unit.connectors

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.domain.DataStoreRequest
import uk.gov.hmrc.customs.emailfrontend.unit.controllers.ControllerSpec
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.Future

class CustomsDataStoreConnectorSpec extends ControllerSpec {

  private val mockHttp = mock[HttpClient]
  private val mockAuditable = mock[Auditable]
  implicit val hc = new HeaderCarrier()
  val testConnector = new CustomsDataStoreConnector(appConfig, mockHttp, mockAuditable)

  "CustomsDataStoreConnector" should {
    "successfully send a query request to customs data store and return the OK response" in {
      when(mockHttp.doPost(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(200)))
      doNothing().when(mockAuditable).sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
      val result = await(testConnector.storeEmailAddress(DataStoreRequest("eori", "emailaddress")))
      result.status shouldBe 200
    }
    "return the failure response from customs data store" in {
      when(mockHttp.doPost(any(), any(), any())(any(), any())).thenReturn(Future.successful(HttpResponse(400)))
      doNothing().when(mockAuditable).sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
      val result = await(testConnector.storeEmailAddress(DataStoreRequest("", "")))
      result.status shouldBe 400
    }
  }
}
