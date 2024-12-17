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

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.customs.emailfrontend.Utils.stripWhiteSpaces
import uk.gov.hmrc.customs.emailfrontend.forms.Validation._
import uk.gov.hmrc.customs.emailfrontend.model.{Email, VerifyChange, YesNo}

object Forms {

  val emailForm: Form[Email] =
    Form(
      mapping("email" -> text.verifying(isValidEmail).transform(stripWhiteSpaces, identity[String]))(Email.apply)(
        email => Some(email.value)
      )
    )

  val confirmEmailForm: Form[YesNo] =
    Form(
      mapping(
        "isYes" -> optional(boolean)
          .verifying(validYesNo("customs.emailfrontend.errors.valid-confirm-email"))
      )(YesNo.apply)(yesNo => Some(yesNo.isYes))
    )

  val confirmVerifyChangeForm: Form[VerifyChange] =
    Form(
      mapping(
        "isVerify" -> optional(boolean)
          .verifying(validVerifyChange("customs.emailfrontend.errors.verify-change"))
      )(VerifyChange.apply)(verifyChange => Some(verifyChange.isVerify))
    )
}
