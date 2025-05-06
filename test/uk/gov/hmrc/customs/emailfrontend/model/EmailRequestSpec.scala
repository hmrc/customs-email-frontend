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

import play.api.libs.json.*
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase

class EmailRequestSpec extends SpecBase {

  "Json Reads" should {
    "generate correct output" in new Setup {
      import EmailRequest.emailRequestFormat

      Json.fromJson(Json.parse(emailRequestJsonString)) shouldBe JsSuccess(emailReq)
    }
  }

  "Json Writes" should {
    "generate correct output" in new Setup {
      Json.toJson(emailReq) shouldBe Json.parse(emailRequestJsonString)
    }
  }

  trait Setup {

    val emails: List[EmailAddress] = List(
      EmailAddress("email1@example.co.uk"),
      EmailAddress("email2@example.co.uk")
    )

    val id: String                  = "template_for_duty_deferment_email"
    val params: Map[String, String] = Map("param1" -> "value1", "param2" -> "value2")
    val enrolment: String           = "testEori"
    val eventUrl: String            = "event.url.co.uk"
    val onSendUrl: String           = "on.send.url.co.uk"

    val emailReq: EmailRequest =
      EmailRequest(emails, id, params, false, Option(enrolment), Option(eventUrl), Option(onSendUrl))

    val emailRequestJsonString: String =
      s"""{
         |  "to": [
         |    "email1@example.co.uk",
         |    "email2@example.co.uk"
         |  ],
         |  "templateId": "$id",
         |  "parameters": {
         |    "param1": "value1",
         |    "param2": "value2"
         |  },
         |  "force": false,
         |  "enrolment": "$enrolment",
         |  "eventUrl": "$eventUrl",
         |  "onSendUrl": "$onSendUrl"
         |}""".stripMargin
  }
}
