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

package uk.gov.hmrc.customs.emailfrontend.model

import org.scalatest.matchers.should.Matchers.*
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.customs.emailfrontend.forms.Forms
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.testEmail

class FormsSpec extends PlaySpec {

  "Forms" should {
    "bind/unbind the emailForm data correctly" in {
      val bind = Forms.emailForm.mapping.bind(Map("email" -> testEmail))
      bind shouldBe Right(Email(testEmail))

      val unbind = Forms.emailForm.mapping.unbind(Email(testEmail))
      unbind shouldBe Map("email" -> testEmail)
    }

    "bind/unbind the confirmEmailForm data correctly" in {
      val bind = Forms.confirmEmailForm.mapping.bind(Map("isYes" -> "true"))
      bind shouldBe Right(YesNo(Some(true)))

      val unbind = Forms.confirmEmailForm.mapping.unbind(YesNo(Some(true)))
      unbind shouldBe Map("isYes" -> "true")
    }
  }
}
