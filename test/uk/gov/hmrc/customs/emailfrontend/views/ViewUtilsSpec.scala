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

package uk.gov.hmrc.customs.emailfrontend.views

import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.confirmVerifyChangeForm
import uk.gov.hmrc.customs.emailfrontend.model.VerifyChange
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString

class ViewUtilsSpec extends SpecBase {
  "title" must {
    "return correct title string when form has no error" in new SetUp {
      ViewUtils.title(formWithNoError, "View Page") shouldBe messages("View Page")
    }

    "return correct title string when form has error" in new SetUp {
      ViewUtils.title(formWithError, "View Page") shouldBe s"${messages("site.errorPrefix")} ${messages("View Page")}"
    }
  }

  "titleWithoutForm" must {
    "return correct title string" in new SetUp {
      ViewUtils.titleWithoutForm("browser.title.msg") shouldBe messages("browser.title.msg")
    }
  }

  "errorPrefix" must {
    "return error prefix when form has any error" in new SetUp {
      ViewUtils.errorPrefix(formWithError) shouldBe messages("site.errorPrefix")
      ViewUtils.errorPrefix(formWithError, "error.prefix.msg") shouldBe messages("error.prefix.msg")
    }

    "return empty string when form has no error" in new SetUp {
      ViewUtils.errorPrefix(formWithNoError) shouldBe emptyString
      ViewUtils.errorPrefix(formWithNoError, "error.prefix.msg") shouldBe emptyString
    }
  }
}

trait SetUp {
  val mapValues_1: Map[String, String] = Map("isVerify" -> "true")
  val mapValues_2: Map[String, String] = Map("isVerify" -> "None")

  val formWithNoError: Form[VerifyChange] = confirmVerifyChangeForm.bind(mapValues_1)
  val formWithError: Form[VerifyChange] = confirmVerifyChangeForm.bind(mapValues_2)

  implicit val messages: Messages = Helpers.stubMessages()
}
