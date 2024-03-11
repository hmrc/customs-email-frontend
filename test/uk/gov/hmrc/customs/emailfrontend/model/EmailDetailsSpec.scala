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

import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EmailDetailsSpec extends PlaySpec {

  "serialize and deserialize to/from JSON properly for correct datetime value in EmailDetails" in new Setup {

    val json = Json.toJson(emailDetails01)

    json shouldBe correctEmailDetailsJson

    val parsedEmailDetails = json.as[EmailDetails]

    parsedEmailDetails shouldBe emailDetails01
  }

  "deserialization from JSON should fail for invalid datetime value in EmailDetails" in new Setup {
    intercept[Exception] {
      inCorrectEmailDetailsJson.as[EmailDetails]
    }
  }

  trait Setup {

    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val validDateTime = "2024-03-11T14:30:00Z"
    val emailDetails01 = EmailDetails(Some("old@example.com"), "new@example.com",
      Some(LocalDateTime.parse(validDateTime, formatter)))

    val correctEmailDetailsJson = Json.obj(
      "currentEmail" -> "old@example.com",
      "newEmail" -> "new@example.com",
      "timestamp" -> validDateTime)

    val inCorrectEmailDetailsJson = Json.obj(
      "currentEmail" -> "old@example.com",
      "newEmail" -> "new@example.com",
      "timestamp" -> "2024-03-11T14:30:00.123456789Z")
  }
}
