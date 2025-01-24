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

package uk.gov.hmrc.customs.emailfrontend.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.*
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.connectors.CustomsDataStoreConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.BadRequest
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.{dateFormatter02, testEmail, testEori, testUtcTimestampMillis}
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.http.{BadRequestException, HttpResponse}

import java.time.LocalDateTime
import scala.concurrent.Future

class CustomsDataStoreServiceSpec extends SpecBase {

  "Customs Data Store Service" should {
    "return a status NO_CONTENT when data store request is successful" in new Setup {

      when(mockConnector.storeEmailAddress(any, any, any)(any))
        .thenReturn(Future.successful(Right(HttpResponse(NO_CONTENT, emptyString))))

      val result = service.storeEmail(enrolmentIdentifier, testEmail, dateTime).futureValue
      result.toOption.get.status shouldBe NO_CONTENT
    }
  }

  "return a status BAD_REQUEST when data store request is successful" in new Setup {

    when(mockConnector.storeEmailAddress(any, any, any)(any))
      .thenReturn(Future.successful(Left(BadRequest)))

    val result = service.storeEmail(enrolmentIdentifier, testEmail, dateTime).futureValue
    result.swap.getOrElse(BadRequest) shouldBe BadRequest
  }

  trait Setup {
    protected val mockConnector       = mock[CustomsDataStoreConnector]
    protected val service             = new CustomsDataStoreService(mockConnector)
    protected val enrolmentIdentifier = EnrolmentIdentifier("EORINumber", testEori)
    protected val dateTime            = LocalDateTime.parse(testUtcTimestampMillis, dateFormatter02)
    protected val badRequestException = new BadRequestException("testMessage")
  }
}
