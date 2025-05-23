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

import play.api.libs.json.Json
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.{dateFormatter01, testEmail, testEmail2, testUtcTimestamp}

import java.time.LocalDateTime

class EmailDetailsSpec extends SpecBase {

  "reads and writes from/to JSON properly for correct datetime value in EmailDetails" in new Setup {

    val json = Json.toJson(emailDetails01)

    json shouldBe correctEmailDetailsJson

    val parsedEmailDetails = json.as[EmailDetails]

    parsedEmailDetails shouldBe emailDetails01
  }

  "reads from JSON should fail for invalid datetime value in EmailDetails" in new Setup {
    intercept[Exception] {
      inCorrectEmailDetailsJson.as[EmailDetails]
    }
  }

  trait Setup {
    val emailDetails01 = EmailDetails(
      Some(testEmail),
      testEmail2,
      Some(LocalDateTime.parse(testUtcTimestamp, dateFormatter01))
    )

    val correctEmailDetailsJson =
      Json.obj("currentEmail" -> testEmail, "newEmail" -> testEmail2, "timestamp" -> testUtcTimestamp)

    val inCorrectEmailDetailsJson = Json.obj(
      "currentEmail" -> testEmail,
      "newEmail"     -> testEmail2,
      "timestamp"    -> "2024-03-11T14:30:00.123456789Z"
    )
  }
}
