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

package uk.gov.hmrc.customs.emailfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.customs.emailfrontend.audit.Auditable
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.connectors.EmailVerificationKeys._
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.EmailVerificationRequestResponse
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationStateHttpParser.EmailVerificationStateResponse
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationConnector @Inject()(http: HttpClient, appConfig: AppConfig, auditable: Auditable)(
  implicit ec: ExecutionContext) {

  private[connectors] lazy val checkVerifiedEmailUrl: String =
    s"${appConfig.emailVerificationWithContext}/verified-email-check"

  private[connectors] lazy val createEmailVerificationRequestUrl: String =
    s"${appConfig.emailVerificationWithContext}/verification-requests"

  def getEmailVerificationState(emailAddress: String)
                               (implicit hc: HeaderCarrier): Future[EmailVerificationStateResponse] = {
    auditRequest("customs-update-email-verification-state", "CustomsUpdateEmailVerificationState", emailAddress, checkVerifiedEmailUrl)
    http.POST[JsObject, EmailVerificationStateResponse](checkVerifiedEmailUrl, Json.obj("email" -> emailAddress))
  }

  def createEmailVerificationRequest(details: EmailDetails, continueUrl: String, eoriNumber: String)
                                    (implicit hc: HeaderCarrier): Future[EmailVerificationRequestResponse] = {
    val jsonBody =
      Json.obj(
        EmailKey -> details.newEmail,
        TemplateIdKey -> appConfig.emailVerificationTemplateId,
        TemplateParametersKey -> Json.obj(),
        LinkExpiryDurationKey -> appConfig.emailVerificationLinkExpiryDuration,
        ContinueUrlKey -> continueUrl
      )

    auditVerificationRequest(details, createEmailVerificationRequestUrl, eoriNumber)

    http.POST[JsObject, EmailVerificationRequestResponse](createEmailVerificationRequestUrl, jsonBody)
  }

  private def auditRequest(transactionName: String, auditType: String, emailAddress: String, url: String)(implicit hc: HeaderCarrier): Unit = {
    auditable.sendDataEvent(
      transactionName = transactionName,
      path = url,
      detail = Map("emailAddress" -> emailAddress),
      auditType = auditType
    )
  }

  private def auditVerificationRequest(details: EmailDetails, url: String, eoriNumber: String)(implicit hc: HeaderCarrier): Unit = {
    details.currentEmail.fold(
      auditable.sendDataEvent(
        transactionName = "UpdateVerifiedEmailRequestSubmitted",
        path = url,
        detail = Map("newEmailAddress" -> details.newEmail, "eori" -> eoriNumber),
        auditType = "changeEmailAddressAttempted"
      )
    )(emailAddress =>
      auditable.sendDataEvent(
        transactionName = "UpdateVerifiedEmailRequestSubmitted",
        path = url,
        detail = Map("currentEmailAddress" -> emailAddress, "newEmailAddress" -> details.newEmail, "eori" -> eoriNumber),
        auditType = "changeEmailAddressAttempted"
      )
    )
  }
}

object EmailVerificationKeys {
  val EmailKey = "email"
  val TemplateIdKey = "templateId"
  val TemplateParametersKey = "templateParameters"
  val LinkExpiryDurationKey = "linkExpiryDuration"
  val ContinueUrlKey = "continueUrl"
}
