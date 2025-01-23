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
import org.mockito.Mockito.{reset, when}
import uk.gov.hmrc.customs.emailfrontend.connectors.UpdateVerifiedEmailConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.{HttpErrorResponse, ServiceUnavailable, VerifiedEmailRequest, VerifiedEmailResponse}
import uk.gov.hmrc.customs.emailfrontend.model.*
import uk.gov.hmrc.customs.emailfrontend.model.MessagingServiceParam.*
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.dateFormatter02
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDateTime
import scala.concurrent.Future

class UpdateVerifiedEmailServiceSpec extends SpecBase {

  override implicit lazy val hc: HeaderCarrier = mock[HeaderCarrier]
  private val mockConnector      = mock[UpdateVerifiedEmailConnector]
  val service                    = new UpdateVerifiedEmailService(mockConnector)

  private val eoriNumber = "GBXXXXXXXXXXXX"
  private val email      = "test@email.com"
  private val dateTime   = LocalDateTime.parse("2021-01-01T11:11:11.111Z", dateFormatter02)

  private val bundleIdUpdateVerifiedEmailResponse = VerifiedEmailResponse(
    UpdateVerifiedEmailResponse(
      ResponseCommon("OK", None, dateTime, List(MessagingServiceParam(formBundleIdParamName, "testValue")))
    )
  )

  private val businessErrorUpdateVerifiedEmailResponse = VerifiedEmailResponse(
    UpdateVerifiedEmailResponse(
      ResponseCommon(
        "OK",
        Some("004 - Duplicate Acknowledgement Reference"),
        dateTime,
        List(MessagingServiceParam(positionParamName, Fail))
      )
    )
  )

  private val serviceUnavailableResponse = ServiceUnavailable

  override def beforeEach(): Unit = reset(mockConnector)

  def mockGetEmailVerificationState(response: Either[HttpErrorResponse, VerifiedEmailResponse]): Unit =
    when(
      mockConnector.updateVerifiedEmail(any[VerifiedEmailRequest], any[Option[String]])(any[HeaderCarrier])
    ) thenReturn Future
      .successful(response)

  "Calling UpdateVerifiedEmailService updateVerifiedEmail" should {
    "return Some(true) when VerifiedEmailResponse returned with bundleId" in {

      mockGetEmailVerificationState(Right(bundleIdUpdateVerifiedEmailResponse))

      service
        .updateVerifiedEmail(None, email, eoriNumber, dateTime)
        .futureValue shouldBe Some(true)
    }

    "return None when VerifiedEmailResponse returned without bundleId" in {

      mockGetEmailVerificationState(Right(businessErrorUpdateVerifiedEmailResponse))

      service
        .updateVerifiedEmail(None, email, eoriNumber, dateTime)
        .futureValue shouldBe Some(false)
    }

    "return None when HttpErrorResponse returned" in {

      mockGetEmailVerificationState(Left(serviceUnavailableResponse))

      service
        .updateVerifiedEmail(None, email, eoriNumber, dateTime)
        .futureValue shouldBe None
    }
  }
}
