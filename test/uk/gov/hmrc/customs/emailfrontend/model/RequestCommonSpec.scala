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
import play.api.libs.json.Json

import java.time.LocalDateTime

class RequestCommonSpec extends PlaySpec {

  "reads and writes from/to JSON properly for correct datetime value in RequestCommon" in new Setup {

    val json = Json.toJson(requestCmn01)

    (json \ "regime") shouldBe (correctEmailDetailsJson \ "regime")

    val parsedReqCmn = json.as[RequestCommon]

    parsedReqCmn.regime shouldBe requestCmn01.regime
  }

  "reads from JSON should fail for invalid datetime value in RequestCommon" in new Setup {
    intercept[Exception] {
      inCorrectEmailDetailsJson.as[RequestCommon]
    }
  }

  trait Setup {

    val dataTimeObj01: LocalDateTime = LocalDateTime.now()
    val requestCmn01                 = RequestCommon("CDS", dataTimeObj01, "sampleguid")

    val correctEmailDetailsJson =
      Json.obj("regime" -> "CDS", "receiptDate" -> dataTimeObj01, "acknowledgementReference" -> "sampleguid")

    val inCorrectEmailDetailsJson = Json.obj(
      "regime"                   -> "CDS",
      "receiptDate"              -> "2024-03-11T14:30:00.123456789Z",
      "acknowledgementReference" -> "sampleguid"
    )
  }
}
