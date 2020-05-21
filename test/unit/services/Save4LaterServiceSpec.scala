/*
 * Copyright 2020 HM Revenue & Customs
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

package unit.services

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.connectors.Save4LaterConnector
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, InternalId, ReferrerName}
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class Save4LaterServiceSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach with ScalaFutures {
  private val mockSave4LaterConnector = mock[Save4LaterConnector]
  private implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private val internalId = InternalId("internalId-123")
  private val timestamp = DateTimeUtil.dateTime
  private val emailDetails = EmailDetails(None, "test@test.com", Some(timestamp))
  private val defaultTimeout: FiniteDuration = 5 seconds
  private val emailKey = "email"
  private val referrerKey = "referrer"
  private val referrerName = ReferrerName("customs-finance", "/xyz")
  private val service =
    new Save4LaterService(mockSave4LaterConnector)

  override implicit def patienceConfig: PatienceConfig =
    super.patienceConfig.copy(timeout = Span(defaultTimeout.toMillis, Millis))

  override protected def beforeEach(): Unit =
    reset(mockSave4LaterConnector)

  "Save4LaterService" should {
    "save the emailDetails against the users InternalId" in {
      when(
        mockSave4LaterConnector.put[EmailDetails](
          ArgumentMatchers.eq(internalId.id),
          ArgumentMatchers.eq(emailKey),
          ArgumentMatchers.eq(emailDetails)
        )(any[HeaderCarrier], any[Reads[EmailDetails]], any[Writes[EmailDetails]])
      ).thenReturn(Future.successful(()))

      val result: Unit = service
        .saveEmail(internalId, emailDetails)
        .futureValue
      result mustBe (())
    }

    "fetch the emailDetails for the users InternalId" in {
      when(
        mockSave4LaterConnector.get[EmailDetails](ArgumentMatchers.eq(internalId.id), ArgumentMatchers.eq(emailKey))(
          any[HeaderCarrier],
          any[Reads[EmailDetails]],
          any[Writes[EmailDetails]]
        )
      ).thenReturn(Future.successful(Some(emailDetails)))

      val result = service
        .fetchEmail(internalId)
        .futureValue
      result mustBe Some(emailDetails)
    }

    "save the referrer against the users InternalId" in {
      when(
        mockSave4LaterConnector.put[ReferrerName](
          ArgumentMatchers.eq(internalId.id),
          ArgumentMatchers.eq(referrerKey),
          ArgumentMatchers.eq(referrerName)
        )(any[HeaderCarrier], any[Reads[ReferrerName]], any[Writes[ReferrerName]])
      ).thenReturn(Future.successful(()))

      val result: Unit = service
        .saveReferrer(internalId, referrerName)
        .futureValue
      result mustBe (())
    }

    "fetch the referrer for the users InternalId" in {
      when(
        mockSave4LaterConnector.get[ReferrerName](ArgumentMatchers.eq(internalId.id), ArgumentMatchers.eq(referrerKey))(
          any[HeaderCarrier],
          any[Reads[ReferrerName]],
          any[Writes[ReferrerName]]
        )
      ).thenReturn(Future.successful(Some(referrerName)))

      val result = service
        .fetchReferrer(internalId)
        .futureValue
      result mustBe Some(referrerName)
    }

    "remove the id" in {
      when(mockSave4LaterConnector.delete(ArgumentMatchers.eq(internalId.id))(any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      val result = service
        .remove(internalId)
        .futureValue
      result mustBe (())
    }

  }

}
