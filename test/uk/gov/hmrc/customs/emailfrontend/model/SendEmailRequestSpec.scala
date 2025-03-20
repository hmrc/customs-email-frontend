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

import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import play.api.libs.json.Json
import org.scalatest.matchers.must.Matchers.mustBe

class SendEmailRequestSpec extends SpecBase {

  "emailRequestFormat" should {

    "generate correct object for Json Reads" in new Setup {
      import SendEmailRequest.emailRequestFormat

      Json.fromJson(Json.parse(sendMailReqJsString)).get mustBe sendEmailReq
    }

    "generate correct Json for Json Writes" in new Setup {
      Json.toJson(sendEmailReq) mustBe Json.parse(sendMailReqJsString)
    }
  }

  trait Setup {
    val destMailId   = "test@test.com"
    val emailAddress = "abcltd@gmail.com"

    val sendEmailReq: SendEmailRequest =
      SendEmailRequest(
        to = Seq(destMailId),
        templateId = "customs_financials_change_email",
        parameters = Map("emailAddress" -> emailAddress)
      )

    val sendMailReqJsString: String =
      """{
        |"to":["test@test.com"],
        |"templateId":"customs_financials_change_email",
        |"parameters":{"emailAddress":"abcltd@gmail.com"}
        |}""".stripMargin
  }
}
