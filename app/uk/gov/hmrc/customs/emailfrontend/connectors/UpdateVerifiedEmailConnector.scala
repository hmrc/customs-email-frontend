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

package uk.gov.hmrc.customs.emailfrontend.connectors

import javax.inject.Inject
import play.api.Logger
import play.mvc.Http.Status._
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses._
import uk.gov.hmrc.http.{ForbiddenException, _}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

class UpdateVerifiedEmailConnector @Inject()(appConfig: AppConfig, http: HttpClient, audit: Auditable) {

  private[connectors] lazy val url: String = appConfig.updateVerifiedEmailUrl

  def updateVerifiedEmail(request: VerifiedEmailRequest)(implicit hc: HeaderCarrier): Future[Either[HttpErrorResponse, VerifiedEmailResponse]] = {

    val details = Map(
      "requestDetail" -> request.updateVerifiedEmailRequest.requestDetail.toString,
      "requestCommon" -> request.updateVerifiedEmailRequest.requestCommon.toString
    )

    auditRequest(details)

    http.PUT[VerifiedEmailRequest, VerifiedEmailResponse](url, request) map { resp =>
      auditResponse(Map("responseCommon" -> resp.updateVerifiedEmailResponse.responseCommon.toString))
      Right(resp)
    } recover {
      case _: BadRequestException | Upstream4xxResponse(_, BAD_REQUEST, _, _) => Left(BadRequest)
      case _: ForbiddenException | Upstream4xxResponse(_, FORBIDDEN, _, _) => Left(Forbidden)
      case _: InternalServerException | Upstream5xxResponse(_, INTERNAL_SERVER_ERROR, _) => Left(ServiceUnavailable)
      case NonFatal(e) =>
        auditResponse(Map("HttpErrorResponse" -> e.getMessage))
        Logger.error(s"[UpdateVerifiedEmailConnector][updateVerifiedEmail] update-verified-email. url: $url, error: ${e.getMessage}")
        Left(UnhandledException)
    }
  }

  private def auditRequest(detail: Map[String, String])(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(
      transactionName = "UpdateVerifiedEmailRequestSubmitted",
      path = url,
      detail = detail,
      auditType = "UpdateVerifiedEmailRequest"
    )

  private def auditResponse(detail: Map[String, String])(implicit hc: HeaderCarrier): Unit =
    audit.sendDataEvent(
      transactionName = "UpdateVerifiedEmailResponseReceived",
      path = url,
      detail = detail,
      auditType = "UpdateVerifiedEmailResponse"
    )
}
