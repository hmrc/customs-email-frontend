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

package uk.gov.hmrc.customs.emailfrontend.services

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.http.Status._
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.services.CustomsDataStoreService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomsDataStoreServiceSpec
    extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterEach
    with ScalaFutures {

  private val mockConnector = mock[CustomsDataStoreConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val service = new CustomsDataStoreService(mockConnector)

  val enrolmentIdentifier = EnrolmentIdentifier("EORINumber", "GB123456789")
  val email = "abc@def.com"
  val dateTime = DateTime.parse("2021-01-01T11:11:11.111Z")
  override protected def beforeEach(): Unit =
    reset(mockConnector)

  "Customs Data Store Service" should {
    "return a status OK when data store request is successful" in {
      when(mockConnector.storeEmailAddress(any(), any(), any())(any()))
        .thenReturn(Future.successful(HttpResponse(OK, "")))

      service
        .storeEmail(enrolmentIdentifier, email, dateTime)
        .futureValue
        .status mustBe OK
    }
  }

  "return a status BAD_REQUEST when data store request is successful" in {
    when(mockConnector.storeEmailAddress(any(), any(), any())(any()))
      .thenReturn(Future.successful(HttpResponse(BAD_REQUEST, "")))

    service
      .storeEmail(enrolmentIdentifier, email, dateTime)
      .futureValue
      .status mustBe BAD_REQUEST
  }
}
