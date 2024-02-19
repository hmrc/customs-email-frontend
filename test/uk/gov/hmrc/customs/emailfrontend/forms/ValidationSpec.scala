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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.data.validation.{Invalid, Valid, ValidationError}
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.{emptyString, singleSpace}

class ValidationSpec extends SpecBase {

  "validEmail" should {
    "return correct result" when {
      "email is valid when emailWithLeadingSpaces" in new SetUp {
        Validation.isValidEmail(emailWithLeadingSpaces) mustBe Valid
      }

      "email is valid when emailWithTrailingSpaces" in new SetUp {
        Validation.isValidEmail(emailWithTrailingSpaces) mustBe Valid
      }

      "email is valid when emailWithLeadingAndTrailingSpaces" in new SetUp {
        Validation.isValidEmail(emailWithLeadingAndTrailingSpaces) mustBe Valid
      }

      "email is valid when emailWithSpaces before @" in new SetUp {
        Validation.isValidEmail(emailWithSpacesWithIn_1) mustBe Valid
      }

      "email is valid when emailWithSpacees after @" in new SetUp {
        Validation.isValidEmail(emailWithSpacesWithIn_2) mustBe Valid
      }

      "email is valid when emailWithSpaces in domain" in new SetUp {
        Validation.isValidEmail(emailWithSpacesWithIn_3) mustBe Valid
      }

      "email is valid when emailWithSpaces spiltting name" in new SetUp {
        Validation.isValidEmail(emailWithSpacesWithIn_4) mustBe Valid
      }
    }

    "return invalid" when {
      "email is invalid email does not contain a .XYZ" in new SetUp {
        Validation.isValidEmail(invalidEmail_1) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.wrong-format"))))
      }

      "email is invalid when email has no @ or .XYZ" in new SetUp {
        Validation.isValidEmail(invalidEmail_2) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.wrong-format"))))
      }

      "email is invalid when email has not @" in new SetUp {
        Validation.isValidEmail(invalidEmail_3) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.wrong-format"))))
      }

      "email is invalid when email has no @ or front" in new SetUp {
        Validation.isValidEmail(invalidEmail_4) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.wrong-format"))))
      }

      "email is invalid when email is to long" in new SetUp {
        Validation.isValidEmail(invalidEmail_5) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.too-long"))))
      }

      "email is invalid when email is empty" in new SetUp {
        Validation.isValidEmail(invalidEmail_6) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.empty"))))
      }

      "email is invalid email is just white space" in new SetUp {
        Validation.isValidEmail(invalidEmail_7) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.empty"))))
      }
    }
  }
}

trait SetUp {
  val spaces = "   "
  val emailWithLeadingSpaces: String = spaces.concat("abc@test.com")
  val emailWithTrailingSpaces: String = "abc@test.com".concat(spaces)
  val emailWithLeadingAndTrailingSpaces: String = spaces.concat("abc@test.com").concat(spaces)

  val emailWithSpacesWithIn_1 = "abc @test.com"
  val emailWithSpacesWithIn_2 = "abc@ test.com"
  val emailWithSpacesWithIn_3 = "abc@te  st.com"
  val emailWithSpacesWithIn_4 = "ab c@test.com"

  val invalidEmail_1 = "first@last"
  val invalidEmail_2 = "firstlast"
  val invalidEmail_3 = "first.com"
  val invalidEmail_4 = ".com"
  val invalidEmail_5 = "thisemailaddressisgreaterthan50charactershenceinvalid.com"
  val invalidEmail_6 = emptyString
  val invalidEmail_7 = singleSpace
}
