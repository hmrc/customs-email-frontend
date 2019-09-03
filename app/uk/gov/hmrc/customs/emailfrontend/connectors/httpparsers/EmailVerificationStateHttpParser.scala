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

package uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers

import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object EmailVerificationStateHttpParser {

  type EmailVerificationStateResponse = Either[EmailVerificationStateErrorResponse, EmailVerificationState]

  implicit object GetEmailVerificationStateHttpReads extends HttpReads[EmailVerificationStateResponse] {
    override def read(method: String, url: String, response: HttpResponse): EmailVerificationStateResponse =
      response.status match {
        case OK =>
          Logger.debug(
            "[GetEmailVerificationStateHttpParser][GetEmailVerificationStateHttpReads][read] - Email Verified"
          )
          Right(EmailVerified)
        case NOT_FOUND =>
          Logger.warn(
            "[GetEmailVerificationStateHttpParser][GetEmailVerificationStateHttpReads][read] - Email not verified"
          )
          Right(EmailNotVerified)
        case status =>
          Logger.warn(
            s"[GetEmailVerificationStateHttpParser][GetEmailVerificationStateHttpReads][read] - " +
              s"Unexpected Response, Status $status returned, with response: ${response.body}"
          )
          Left(EmailVerificationStateErrorResponse(status, response.body))
      }
  }

  sealed trait EmailVerificationState

  case object EmailVerified extends EmailVerificationState

  case object EmailNotVerified extends EmailVerificationState

  case class EmailVerificationStateErrorResponse(status: Int, body: String)
}
