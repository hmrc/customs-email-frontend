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
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.VerifiedEmailResponse

class UpdateVerifiedEmailResponseSpec extends PlaySpec {

  "UpdateVerifiedEmailResponse" should {

    "parse the json to model VerifiedEmailResponse" in {
      Json.parse("""{
                     |  "updateVerifiedEmailResponse": {
                     |    "responseCommon": {
                     |      "status": "OK",
                     |      "processingDate": "2019-08-22T13:55:53Z",
                     |      "returnParameters": [
                     |        {
                     |          "paramName": "ETMPFORMBUNDLENUMBER",
                     |          "paramValue": "093000001830"
                     |        }
                     |      ]
                     |    }
                     |  }
                     |}""".stripMargin).as[VerifiedEmailResponse]

    }

    "parse the json to model VerifiedEmailResponse when status text is available" in {

      Json
        .parse("""{
          |  "updateVerifiedEmailResponse": {
          |    "responseCommon": {
          |      "status": "OK",
          |      "statusText": "004 - Duplicate Acknowledgement Reference",
          |      "processingDate": "2016-08-17T19:33:47Z",
          |      "returnParameters": [
          |        {
          |          "paramName": "POSITION",
          |          "paramValue": "FAIL"
          |        }
          |      ]
          |    }
          |  }
          |}""".stripMargin)
        .as[VerifiedEmailResponse]
    }
    "parse the json to model VerifiedEmailResponse when returnParameter is Nil should throw exception" in {

      intercept[IllegalArgumentException] {
        Json
          .parse(
            """{
                       |  "updateVerifiedEmailResponse": {
                       |    "responseCommon": {
                       |      "status": "OK",
                       |      "statusText": "004 - Duplicate Acknowledgement Reference",
                       |      "processingDate": "2016-08-17T19:33:47Z",
                       |      "returnParameters": []
                       |    }
                       |  }
                       |}""".stripMargin)
          .as[VerifiedEmailResponse]
      }
    }
  }
}
