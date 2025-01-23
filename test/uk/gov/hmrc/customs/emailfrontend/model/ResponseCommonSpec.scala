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
import uk.gov.hmrc.customs.emailfrontend.model.MessagingServiceParam.formBundleIdParamName
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.dateFormatter01

import java.time.LocalDateTime

class ResponseCommonSpec extends SpecBase {

  "reads and writes from/to JSON properly for correct ResponseCommon" in new Setup {

    val json = Json.toJson(requestCmn01)

    (json \ "status") shouldBe (correctResponseCmnJson \ "status")

    val parsedReqCmn = json.as[ResponseCommon]

    parsedReqCmn.statusText shouldBe requestCmn01.statusText
  }

  "reads from JSON should fail for invalid datetime in ResponseCommon" in new Setup {
    intercept[Exception] {
      inCorrectResponseCmnJson.as[ResponseCommon]
    }
  }

  trait Setup {

    val status                          = "OK"
    val statusText                      = Some("200 - OK")
    val validDateTime                   = "2024-03-11T14:30:00Z"
    val invalidDateTime                 = "2024-03-11T14:30:00.123456789Z"
    val validDateTimeObj: LocalDateTime = LocalDateTime.parse(validDateTime, dateFormatter01)
    val returnParams                    = List(MessagingServiceParam(formBundleIdParamName, "testValue"))
    val requestCmn01                    = ResponseCommon(status, statusText, validDateTimeObj, returnParams)

    val correctResponseCmnJson = Json.obj(
      "status"           -> status,
      "statusText"       -> statusText,
      "processingDate"   -> validDateTime,
      "returnParameters" -> returnParams
    )

    val inCorrectResponseCmnJson = Json.obj(
      "status"           -> status,
      "statusText"       -> statusText,
      "processingDate"   -> invalidDateTime,
      "returnParameters" -> returnParams
    )
  }
}
