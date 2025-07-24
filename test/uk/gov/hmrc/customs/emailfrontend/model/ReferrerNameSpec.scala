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

import play.api.libs.json.{JsResultException, JsSuccess, Json}
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase

class ReferrerNameSpec extends SpecBase {

  "ReferrerName.formats" should {
    "generate correct output for Json Reads" in new Setup {
      import ReferrerName.formats

      Json.fromJson(Json.parse(referrerNameObJsString)) shouldBe JsSuccess(referrerNameOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"name1\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[ReferrerName]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(referrerNameOb) shouldBe Json.parse(referrerNameObJsString)
    }
  }

  trait Setup {
    val referrerNameOb: ReferrerName = ReferrerName("test_name", "test_url")

    val referrerNameObJsString: String = """{"name":"test_name","continueUrl":"test_url"}""".stripMargin
  }
}
