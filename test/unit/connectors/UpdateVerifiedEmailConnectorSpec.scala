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

import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{doNothing, reset, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Writes
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.UpdateVerifiedEmailConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses._
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, MethodNotAllowedException, _}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.test.Helpers._
import play.mvc.Http.Status.{BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR}

import scala.concurrent.{ExecutionContext, Future}

class UpdateVerifiedEmailConnectorSpec extends PlaySpec
  with ScalaFutures
  with MockitoSugar
  with BeforeAndAfter {

  private val mockAuditable = mock[Auditable]
  private val mockAppConfig = mock[AppConfig]
  private val mockHttpClient = mock[HttpClient]

  private val forbiddenException = new ForbiddenException("testMessage")
  private val badRequestException = new BadRequestException("testMessage")
  private val internalServerException = new InternalServerException("testMessage")
  private val unhandledException = new MethodNotAllowedException("testMessage")

  private val badRequest = Upstream4xxResponse("testMessage", BAD_REQUEST, BAD_REQUEST)
  private val forbidden = Upstream4xxResponse("testMessage", FORBIDDEN, FORBIDDEN)
  private val internalServerError = Upstream5xxResponse("testMessage", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)

  val dateTime = DateTime.now()
  private val requestDetail = RequestDetail("idType", "idNumber", "test@email.com", dateTime)
  private val requestCommon = RequestCommon()

  private val verifiedEmailResponse = VerifiedEmailResponse(UpdateVerifiedEmailResponse(ResponseCommon("OK", None, dateTime, List(MessagingServiceParam("name", "value")))))

  private val updateVerifiedEmailRequest = UpdateVerifiedEmailRequest(requestCommon, requestDetail)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val connector = new UpdateVerifiedEmailConnector(mockAppConfig, mockHttpClient, mockAuditable)

  before {
    reset(mockAuditable, mockAppConfig, mockHttpClient)
    doNothing().when(mockAuditable).sendDataEvent(any(), any(), any(), any())(any[HeaderCarrier])
    when(mockAppConfig.updateVerifiedEmailUrl).thenReturn("testUrl/update-verified-email")
  }

  "Calling updateVerifiedEmail" should {
    "return Right with VerifiedEmailResponse when call was successful with OK" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(verifiedEmailResponse))

      val result = connector.updateVerifiedEmail(updateVerifiedEmailRequest).futureValue
      result mustBe Right(verifiedEmailResponse)
    }

    "return Left with Forbidden when call returned NotFoundException" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(forbiddenException))

      val result = connector.updateVerifiedEmail(updateVerifiedEmailRequest).futureValue
      result mustBe Left(Forbidden)
    }

    "return Left with Forbidden when call returned Upstream4xxResponse with 403" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(forbidden))

      val result = connector.updateVerifiedEmail(updateVerifiedEmailRequest).futureValue
      result mustBe Left(Forbidden)
    }

    "return Left with BadRequest when call returned BadRequestException" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(badRequestException))

      val result = connector.updateVerifiedEmail(updateVerifiedEmailRequest).futureValue
      result mustBe Left(BadRequest)
    }

    "return Left with BadRequest when call returned Upstream4xxResponse with 400" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(badRequest))

      val result = connector.updateVerifiedEmail(updateVerifiedEmailRequest).futureValue
      result mustBe Left(BadRequest)
    }

    "return Left with ServiceUnavailable when call returned ServiceUnavailableException" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(internalServerException))

      val result = connector.updateVerifiedEmail(updateVerifiedEmailRequest).futureValue
      result mustBe Left(ServiceUnavailable)
    }

    "return Left with ServiceUnavailable when call returned Upstream5xxResponse with 500" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(internalServerError))

      val result = connector.updateVerifiedEmail(updateVerifiedEmailRequest).futureValue
      result mustBe Left(ServiceUnavailable)
    }

    "return Left with not handled exception" in {
      when(mockHttpClient.PUT[VerifiedEmailRequest, VerifiedEmailResponse](
        meq("testUrl/update-verified-email"), meq(VerifiedEmailRequest(updateVerifiedEmailRequest)))
        (any[Writes[VerifiedEmailRequest]], any[HttpReads[VerifiedEmailResponse]], any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.failed(unhandledException))

      val result = await(connector.updateVerifiedEmail(updateVerifiedEmailRequest))
      result mustBe Left(UnhandledException)
    }
  }
}

