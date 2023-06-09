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

import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model.Email
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase

class FormsSpec extends SpecBase {
  "emailForm" must {
    "bind the form correctly" in {
      val mapValues_1: Map[String, String] = Map("email" -> "abc@test.com")
      val mapValues_2: Map[String, String] = Map("email" -> "abc@te  st.com")
      val mapValues_3: Map[String, String] = Map("email" -> "a bc@test.com")
      val mapValues_4: Map[String, String] = Map("email" -> "    abc@test.com")
      val mapValues_5: Map[String, String] = Map("email" -> " abc@test.com  ")

      val result1 = emailForm.bind(mapValues_1)
      val result2 = emailForm.bind(mapValues_2)
      val result3 = emailForm.bind(mapValues_3)
      val result4 = emailForm.bind(mapValues_4)
      val result5 = emailForm.bind(mapValues_5)

      result1.value shouldBe Option(Email("abc@test.com"))
      result2.value shouldBe Option(Email("abc@test.com"))
      result3.value shouldBe Option(Email("abc@test.com"))
      result4.value shouldBe Option(Email("abc@test.com"))
      result5.value shouldBe Option(Email("abc@test.com"))
    }
    
    "not bind when value is incorrect" in {

      val mapValues_1: Map[String, String] = Map("email" -> "abctest")
      val mapValues_2: Map[String, String] = Map("email" -> "st.com")
      val mapValues_3: Map[String, String] = Map("email" -> "")
      val mapValues_4: Map[String, String] = Map(
        "email" -> "this value is more than fifty characters long hence invalid for email")

      val result1 = emailForm.bind(mapValues_1)
      val result2 = emailForm.bind(mapValues_2)
      val result3 = emailForm.bind(mapValues_3)
      val result4 = emailForm.bind(mapValues_4)

      result1.errors.size shouldBe 1
      result1.errors.head.message shouldBe "customs.emailfrontend.errors.valid-email.wrong-format"
      result2.errors.size shouldBe 1
      result2.errors.head.message shouldBe "customs.emailfrontend.errors.valid-email.wrong-format"
      result3.errors.size shouldBe 1
      result3.errors.head.message shouldBe "customs.emailfrontend.errors.valid-email.empty"
      result4.errors.size shouldBe 1
      result4.errors.head.message shouldBe "customs.emailfrontend.errors.valid-email.too-long"
    }
  }
}
