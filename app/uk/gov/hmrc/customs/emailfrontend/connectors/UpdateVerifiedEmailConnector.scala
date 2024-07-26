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

package uk.gov.hmrc.customs.emailfrontend.connectors

import play.api.Logging
import play.mvc.Http.Status._
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{ForbiddenException, _}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class UpdateVerifiedEmailConnector @Inject()(appConfig: AppConfig,
                                             http: HttpClientV2,
                                             audit: Auditable)(implicit ec: ExecutionContext) extends Logging {

  def updateVerifiedEmail(request: VerifiedEmailRequest, currentEmail: Option[String])
                         (implicit hc: HeaderCarrier): Future[Either[HttpErrorResponse, VerifiedEmailResponse]] = {

    val newEmail = request.updateVerifiedEmailRequest.requestDetail.emailAddress
    val eori = request.updateVerifiedEmailRequest.requestDetail.IDNumber

    auditRequest(currentEmail, newEmail, eori, "changeEmailAddressVerified")

    http.put(url"${appConfig.updateVerifiedEmailUrl}")
      .withBody[VerifiedEmailRequest](request)
      .execute[VerifiedEmailResponse].map { resp =>
        auditRequest(currentEmail, newEmail, eori, "changeEmailAddressConfirmed")
        Right(resp)
      } recover {
      case _: BadRequestException | UpstreamErrorResponse(_, BAD_REQUEST, _, _) => Left(BadRequest)
      case _: ForbiddenException | UpstreamErrorResponse(_, FORBIDDEN, _, _) => Left(Forbidden)
      case _: InternalServerException | UpstreamErrorResponse(_, INTERNAL_SERVER_ERROR, _, _) =>
        Left(ServiceUnavailable)

      case NonFatal(e) =>
        logger.error(s"update-verified-email. url: $appConfig.updateVerifiedEmailUrl, error: ${e.getMessage}")
        Left(UnhandledException)
    }
  }

  private def auditRequest(currentEmail: Option[String], newEmail: String, eoriNumber: String, auditType: String)
                          (implicit hc: HeaderCarrier): Unit =
    currentEmail.fold(
      audit.sendDataEvent(
        transactionName = "UpdateVerifiedEmailRequestSubmitted",
        path = appConfig.updateVerifiedEmailUrl,
        detail = Map("newEmailAddress" -> newEmail, "eori" -> eoriNumber),
        auditType = auditType
      )
    )(
      emailAddress =>
        audit.sendDataEvent(
          transactionName = "UpdateVerifiedEmailRequestSubmitted",
          path = appConfig.updateVerifiedEmailUrl,
          detail = Map("currentEmailAddress" -> emailAddress, "newEmailAddress" -> newEmail, "eori" -> eoriNumber),
          auditType = auditType
        )
    )
}
