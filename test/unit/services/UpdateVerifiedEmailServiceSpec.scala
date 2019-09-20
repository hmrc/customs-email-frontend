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

package unit.services

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.customs.emailfrontend.connectors.UpdateVerifiedEmailConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.{HttpErrorResponse, ServiceUnavailable, VerifiedEmailRequest, VerifiedEmailResponse}
import uk.gov.hmrc.customs.emailfrontend.model.MessagingServiceParam._
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.UpdateVerifiedEmailService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UpdateVerifiedEmailServiceSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach with ScalaFutures {

  implicit val hc: HeaderCarrier = mock[HeaderCarrier]
  private val mockConnector = mock[UpdateVerifiedEmailConnector]

  val service = new UpdateVerifiedEmailService(mockConnector)

  private val eoriNumber = "GBXXXXXXXXXXXX"
  private val email = "test@email.com"
  private val dateTime = DateTime.now()

  private val bundleIdUpdateVerifiedEmailResponse = VerifiedEmailResponse(
    UpdateVerifiedEmailResponse(ResponseCommon("OK", None, dateTime, List(MessagingServiceParam(formBundleIdParamName, "testValue")))))
  private val businessErrorUpdateVerifiedEmailResponse = VerifiedEmailResponse(
    UpdateVerifiedEmailResponse(ResponseCommon("OK", Some("004 - Duplicate Acknowledgement Reference"), dateTime, List(MessagingServiceParam(positionParamName, Fail)))))
  private val serviceUnavailableResponse = ServiceUnavailable

  override protected def beforeEach(): Unit = {
    reset(mockConnector)
  }

  def mockGetEmailVerificationState(response: Either[HttpErrorResponse, VerifiedEmailResponse]): Unit =
    when(mockConnector.updateVerifiedEmail(any[VerifiedEmailRequest])(any[HeaderCarrier])) thenReturn Future.successful(response)

  "Calling UpdateVerifiedEmailService updateVerifiedEmail" should {
    "return Some(true) when VerifiedEmailResponse returned with bundleId" in {
      mockGetEmailVerificationState(Right(bundleIdUpdateVerifiedEmailResponse))

      service.updateVerifiedEmail(email, eoriNumber).futureValue mustBe Some(true)
    }

    "return None when VerifiedEmailResponse returned without bundleId" in {
      mockGetEmailVerificationState(Right(businessErrorUpdateVerifiedEmailResponse))

      service.updateVerifiedEmail(email, eoriNumber).futureValue mustBe None
    }

    "return None when HttpErrorResponse returned" in {
      mockGetEmailVerificationState(Left(serviceUnavailableResponse))

      service.updateVerifiedEmail(email, eoriNumber).futureValue mustBe None
    }
  }
}
