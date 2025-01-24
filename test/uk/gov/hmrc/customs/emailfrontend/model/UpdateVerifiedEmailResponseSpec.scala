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
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.VerifiedEmailResponse
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.testUtcTimestamp

class UpdateVerifiedEmailResponseSpec extends PlaySpec {

  "UpdateVerifiedEmailResponse" should {

    "parse the json to model VerifiedEmailResponse" in {
      Json
        .parse(s"""{
          |  "updateVerifiedEmailResponse": {
          |    "responseCommon": {
          |      "status": "OK",
          |      "processingDate": "$testUtcTimestamp",
          |      "returnParameters": [
          |        {
          |          "paramName": "ETMPFORMBUNDLENUMBER",
          |          "paramValue": "093000001830"
          |        }
          |      ]
          |    }
          |  }
          |}""".stripMargin)
        .as[VerifiedEmailResponse]
    }

    "parse the json to model VerifiedEmailResponse when status text is available" in {

      Json
        .parse(s"""{
            |  "updateVerifiedEmailResponse": {
            |    "responseCommon": {
            |      "status": "OK",
            |      "statusText": "004 - Duplicate Acknowledgement Reference",
            |      "processingDate": "$testUtcTimestamp",
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
          .parse(s"""{
              |  "updateVerifiedEmailResponse": {
              |    "responseCommon": {
              |      "status": "OK",
              |      "statusText": "004 - Duplicate Acknowledgement Reference",
              |      "processingDate": "$testUtcTimestamp",
              |      "returnParameters": []
              |    }
              |  }
              |}""".stripMargin)
          .as[VerifiedEmailResponse]
      }
    }
  }
}
