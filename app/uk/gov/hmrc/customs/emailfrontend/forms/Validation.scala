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

package uk.gov.hmrc.customs.emailfrontend.forms

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import uk.gov.hmrc.customs.emailfrontend.Utils.stripWhiteSpaces

object Validation {

  private val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r

  private def isValid(e: String): Boolean = e match {
    case e if emailRegex.findFirstMatchIn(e).isDefined => true
    case _ => false
  }

  private val validEmailMaxLength = 50

  def isValidEmail: Constraint[String] =
    Constraint({
      case e if Option(e).isEmpty =>
        Invalid(ValidationError("customs.emailfrontend.errors.valid-email.empty"))

      case e if stripWhiteSpaces(e).isEmpty =>
        Invalid(ValidationError("customs.emailfrontend.errors.valid-email.empty"))

      case e if e.length > validEmailMaxLength =>
        Invalid(ValidationError("customs.emailfrontend.errors.valid-email.too-long"))

      case e if !isValid(stripWhiteSpaces(e)) =>
        Invalid(ValidationError("customs.emailfrontend.errors.valid-email.wrong-format"))

      case _ => Valid
    })

  def validYesNo(errorMessage: String): Constraint[Option[Boolean]] =
    Constraint({
      case None => Invalid(ValidationError(errorMessage))
      case _ => Valid
    })

  def validVerifyChange(errorMessage: String): Constraint[Option[Boolean]] =
    Constraint({
      case None => Invalid(ValidationError(errorMessage))
      case _ => Valid
    })
}
