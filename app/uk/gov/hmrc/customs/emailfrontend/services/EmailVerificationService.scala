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

import uk.gov.hmrc.customs.emailfrontend.connectors.EmailVerificationConnector
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationRequestHttpParser.EmailVerificationRequestSuccess
import uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers.EmailVerificationStateHttpParser.{
  EmailNotVerified, EmailVerified
}
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailVerificationService @Inject() (emailVerificationConnector: EmailVerificationConnector)(implicit
  ec: ExecutionContext
) {

  def isEmailVerified(email: String)(implicit hc: HeaderCarrier): Future[Option[Boolean]] =
    emailVerificationConnector.getEmailVerificationState(email).map {
      case Right(EmailVerified)    => Some(true)
      case Right(EmailNotVerified) => Some(false)
      case Left(_)                 => None
    }

  def createEmailVerificationRequest(details: EmailDetails, continueUrl: String, eoriNumber: String)(implicit
    hc: HeaderCarrier
  ): Future[Option[EmailVerificationRequestSuccess]] =
    emailVerificationConnector.createEmailVerificationRequest(details, continueUrl, eoriNumber).map(_.toOption)

}
