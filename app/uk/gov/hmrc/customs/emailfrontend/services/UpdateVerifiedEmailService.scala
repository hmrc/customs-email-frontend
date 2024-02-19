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

import org.joda.time.DateTime
import play.api.Logging
import uk.gov.hmrc.customs.emailfrontend.connectors.UpdateVerifiedEmailConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.VerifiedEmailRequest
import uk.gov.hmrc.customs.emailfrontend.model.MessagingServiceParam._
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UpdateVerifiedEmailService @Inject()(updateVerifiedEmailConnector: UpdateVerifiedEmailConnector)
                                          (implicit ec: ExecutionContext) extends Logging {

  def updateVerifiedEmail(currentEmail: Option[String], newEmail: String, eori: String, timestamp: DateTime)
                         (implicit hc: HeaderCarrier): Future[Option[Boolean]] = {

    val requestDetail = RequestDetail(
      IDType = "EORI",
      IDNumber = eori,
      emailAddress = newEmail,
      emailVerificationTimestamp = timestamp)

    val request = VerifiedEmailRequest(UpdateVerifiedEmailRequest(RequestCommon(), requestDetail))

    updateVerifiedEmailConnector.updateVerifiedEmail(request, currentEmail).map {
      case Right(res)
        if res.updateVerifiedEmailResponse.responseCommon.returnParameters
          .exists(msp => msp.paramName == formBundleIdParamName) =>
        logger.info("Successfully updated verified email")
        Some(true)

      case Right(res) =>
        val statusText = res.updateVerifiedEmailResponse.responseCommon.statusText

        logger.debug(s"Updating verified email unsuccessful with business error/status code:" +
          s" ${statusText.getOrElse("Status text empty")}")

        Some(false)

      case Left(res) =>
        logger.warn(s"Updating verified email unsuccessful with response: $res")
        None
    }
  }
}
