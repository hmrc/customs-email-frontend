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
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Reads
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.connectors.Save4LaterConnector
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, InternalId, ReferrerName}
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.Future

class Save4LaterServiceSpec extends SpecBase with BeforeAndAfterEach {

  trait Setup {
    implicit val hc: HeaderCarrier = mock[HeaderCarrier]
    protected val internalId: InternalId = InternalId("internalId-123")
    protected val timestamp: DateTime = DateTimeUtil.dateTime
    protected val emailDetails: EmailDetails = EmailDetails(None, "test@test.com", Some(timestamp))
    protected val referrerName: ReferrerName = ReferrerName("customs-finance", "/xyz")
    protected val mockSave4LaterConnector = mock[Save4LaterConnector]
    protected val service = new Save4LaterService(mockSave4LaterConnector)
  }

  "Save4LaterService" should {
    "save the emailDetails against the users InternalId" in new Setup {

      when(mockSave4LaterConnector.put[EmailDetails](any, any, any)(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(())))

      val result: Unit = service.saveEmail(internalId, emailDetails).futureValue
      result shouldBe (()) //TODO - ????
    }

    "fetch the emailDetails for the users InternalId" in new Setup {
      when(mockSave4LaterConnector.getEmailDetails(any, any)(any[HeaderCarrier], any[Reads[EmailDetails]])
      ).thenReturn(Future.successful(Some(emailDetails)))

      val result = service.fetchEmail(internalId).futureValue

      result shouldBe Some(emailDetails)
    }

    "save the referrer against the users InternalId" in new Setup {
      when(mockSave4LaterConnector.put[ReferrerName](any, any, any)(any[HeaderCarrier])
      ).thenReturn(Future.successful(Right(())))

      val result: Unit = service.saveReferrer(internalId, referrerName).futureValue
      result shouldBe (())
    }

    "fetch the referrer for the users InternalId" in new Setup {
      when(mockSave4LaterConnector.getReferrerName(any, any)(any[HeaderCarrier], any[Reads[ReferrerName]])
      ).thenReturn(Future.successful(Some(referrerName)))

      val result = service.fetchReferrer(internalId).futureValue
      result shouldBe Some(referrerName)
    }

    "remove the id" in new Setup {
      when(mockSave4LaterConnector.delete(any)(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(())))

      val result: Unit = service.remove(internalId).futureValue
      result shouldBe (())
    }
  }
}
