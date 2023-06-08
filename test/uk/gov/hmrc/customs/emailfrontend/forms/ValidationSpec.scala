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

class ValidationSpec extends SpecBase {

  "validEmail" should {
    "return correct result" when {
      "email is valid" in new SetUp {
        Validation.validEmail(emailWithLeadingSpaces) mustBe Valid
        Validation.validEmail(emailWithTrailingSpaces) mustBe Valid
        Validation.validEmail(emailWithLeadingAndTrailingSpaces) mustBe Valid
        Validation.validEmail(emailWithSpacesWithIn_1) mustBe Valid
        Validation.validEmail(emailWithSpacesWithIn_2) mustBe Valid
        Validation.validEmail(emailWithSpacesWithIn_3) mustBe Valid
      }

      "email is invalid" in new SetUp {
        //Validation.validEmail(invalidEmail_1) mustBe Invalid  // needs to be uncommented once the regex is fixed
        Validation.validEmail(invalidEmail_2) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.wrong-format"))))

        Validation.validEmail(invalidEmail_3) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.wrong-format"))))

        Validation.validEmail(invalidEmail_4) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.wrong-format"))))

        Validation.validEmail(invalidEmail_5) mustBe Invalid(
          List(ValidationError(List("customs.emailfrontend.errors.valid-email.too-long"))))
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
  val invalidEmail_1 = "first@last"
  val invalidEmail_2 = "firstlast"
  val invalidEmail_3 = "first.com"
  val invalidEmail_4 = ".com"
  val invalidEmail_5 = "thisemailaddressisgreaterthan50charactershenceinvalid.com"
}
