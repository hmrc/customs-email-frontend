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

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.Json
import uk.gov.hmrc.customs.emailfrontend.utils.CommonUtils.dateFormatter01
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase

import java.time.LocalDateTime
class UpdateEmailSpec extends SpecBase {

  trait Setup {
    val testDateTime01: LocalDateTime = LocalDateTime.now()
  }

  "UpdateEmail" should {

    "serialize timestamp correctly" in new Setup {
      val eori = Eori("testEori")
      val address = "test@example.com"
      val timestamp = LocalDateTime.parse(testDateTime01.format(dateFormatter01), dateFormatter01)

      val updateEmail = UpdateEmail(eori, address, timestamp)
      val json = Json.toJson(updateEmail)
      val parsedUpdateEmail = json.as[UpdateEmail]

      parsedUpdateEmail shouldBe updateEmail
    }

    "handle invalid JSON" in new Setup {
      val invalidJson = Json.parse("""{"invalidField": "test"}""")
      assertThrows[Exception] {
        invalidJson.as[UpdateEmail]
      }
    }

    "handle different date formats correctly" in new Setup {
      val timestamp: String = LocalDateTime.now.format(dateFormatter01)
      val timestampJson = Json.obj("eori" -> "testEori",
        "address" -> "test@example.com", "timestamp" -> timestamp)
      val parsedUpdateEmail = timestampJson.as[UpdateEmail]

      parsedUpdateEmail.timestamp mustBe a[LocalDateTime]
    }
  }
}
