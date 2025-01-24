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

package utils

import play.api.libs.json.{JsError, JsValue, Json}
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.*

import java.time.LocalDateTime

class UtilsSpec extends SpecBase {

  "emptyString" should {
    "return correct value" in {
      emptyString shouldBe ""
    }
  }

  "hyphen" should {
    "return correct value" in {
      hyphen shouldBe "-"
    }
  }

  "singeSpace" should {
    "return correct value" in {
      singleSpace shouldBe " "
    }
  }

  "writesLocalDateTime" should {

    "correctly write LocalDateTime to string value" in {
      val result: JsValue = writesLocalDateTime(LocalDateTime.now())
      Json.stringify(result) should not be empty
    }
  }

  "readsLocalDateTime" should {

    "correctly parse LocalDateTime and return correct value" in {
      val result = readsLocalDateTime(Json.toJson("2024-03-12T16:34:38Z"))
      result.get shouldBe a[LocalDateTime]
    }

    "throw Exception if wrong datetime format is used" in {
      val result = readsLocalDateTime(Json.toJson("2024-03-11T14:30:00.123456789Z"))
      result shouldBe a[JsError]
    }
  }

}
