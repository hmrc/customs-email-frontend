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

class RequestDetailSpec extends PlaySpec {

  "serialize and deserialize to/from JSON properly for correct value of RequestDetail" in new Setup {

    val json = Json.toJson(requestDtl01)

    (json \ "IDType") shouldBe (correctEmailDetailsJson \ "IDType")

    (json \ "emailVerificationTimestamp") shouldBe (correctEmailDetailsJson \ "emailVerificationTimestamp")

    val parsedReqCmn = json.as[RequestDetail]

    parsedReqCmn.IDType shouldBe requestDtl01.IDType
    parsedReqCmn.emailVerificationTimestamp shouldBe requestDtl01.emailVerificationTimestamp
  }

  "deserialization from JSON should fail for invalid value" in new Setup {
    intercept[Exception] {
      inCorrectEmailDetailsJson.as[RequestDetail]
    }
  }

  trait Setup {

    val idType = "EORI"
    val IdNumber = "GBXXXXXXXXXXXX"
    val emailId = "sample@example.com"
    val validDateTime = "2024-03-11T14:30:00Z"
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val validLocalDateTime: LocalDateTime = LocalDateTime.parse(validDateTime, formatter)
    val incorrectDateTime = "2024-03-11T14:30:00.123456789Z"
    val requestDtl01 = RequestDetail(idType,IdNumber, emailId, validLocalDateTime)

    val correctEmailDetailsJson = Json.obj(
      "IDType" -> idType,
      "IDNumber" -> IdNumber,
      "emailAddress" -> emailId,
      "emailVerificationTimestamp" -> validDateTime)

    val inCorrectEmailDetailsJson = Json.obj(
      "IDType" -> idType,
      "IDNumber" -> IdNumber,
      "emailAddress" -> emailId,
      "emailVerificationTimestamp" -> incorrectDateTime)
  }
}
