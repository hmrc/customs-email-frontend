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

package uk.gov.hmrc.customs.emailfrontend.services

import javax.inject.Inject
import play.api.Logger
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.connectors.UpdateVerifiedEmailConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.VerifiedEmailRequest
import uk.gov.hmrc.customs.emailfrontend.model.MessagingServiceParam._
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UpdateVerifiedEmailService @Inject()(updateVerifiedEmailConnector: UpdateVerifiedEmailConnector)(
  implicit ec: ExecutionContext
) {

  def updateVerifiedEmail(currentEmail: Option[String], newEmail: String, eori: String)(
    implicit hc: HeaderCarrier
  ): Future[Option[Boolean]] = {

    val requestDetail = RequestDetail(
      IDType = "EORI",
      IDNumber = eori,
      emailAddress = newEmail,
      emailVerificationTimestamp = DateTimeUtil.dateTime
    )
    val request = VerifiedEmailRequest(UpdateVerifiedEmailRequest(RequestCommon(), requestDetail))

    updateVerifiedEmailConnector.updateVerifiedEmail(request, currentEmail).map {
      case Right(res)
          if res.updateVerifiedEmailResponse.responseCommon.returnParameters
            .exists(msp => msp.paramName == formBundleIdParamName) =>
        Logger.debug("[UpdateVerifiedEmailService][updateVerifiedEmail] - successfully updated verified email")
        Some(true)
      case Right(res) =>
        val statusText = res.updateVerifiedEmailResponse.responseCommon.statusText
        Logger.debug(
          "[UpdateVerifiedEmailService][updateVerifiedEmail]" +
            s" - updating verified email unsuccessful with business error/status code: ${statusText.getOrElse("Status text empty")}"
        )
        Some(false)
      case Left(res) =>
        Logger.warn(
          s"[UpdateVerifiedEmailService][updateVerifiedEmail] - updating verified email unsuccessful with response: $res"
        )
        None
    }
  }
}
