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
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.{testEmail, testUtcTimestamp}

class SubscriptionDisplayResponseSpec extends PlaySpec {

  private val subscriptionDisplayResponse =
    Json
      .parse(s"""{
        |  "subscriptionDisplayResponse": {
        |    "responseDetail": {
        |      "contactInformation": {
        |        "emailAddress": "$testEmail",
        |        "emailVerificationTimestamp": "$testUtcTimestamp"
        |      }
        |    }
        |  }
        |}""".stripMargin)
      .as[SubscriptionDisplayResponse]

  private val noFormBundleSubscriptionDisplayResponse =
    Json
      .parse(s"""{
        |  "subscriptionDisplayResponse": {
        |    "responseCommon": {
        |      "status": "OK",
        |      "statusText": "005 - No form bundle found",
        |      "processingDate": "$testUtcTimestamp",
        |      "returnParameters": [{
        |          "paramName": "POSITION",
        |          "paramValue": "FAIL"
        |          }]
        |     }
        |    }
        |}""".stripMargin)
      .as[SubscriptionDisplayResponse]

  private val noEmailSubscriptionDisplayResponse =
    Json
      .parse(s"""{
        |  "subscriptionDisplayResponse": {
        |    "responseDetail": {
        |      "contactInformation": {
        |        "emailVerificationTimestamp": "$testUtcTimestamp"
        |      }
        |    }
        |  }
        |}""".stripMargin)
      .as[SubscriptionDisplayResponse]

  "SubscriptionDisplayResponse Object" should {

    "contain email when present in response" in {
      subscriptionDisplayResponse.email                      shouldBe Some(testEmail)
      subscriptionDisplayResponse.emailVerificationTimestamp shouldBe Some(testUtcTimestamp)
    }

    "contain status when present in response" in {
      noFormBundleSubscriptionDisplayResponse.statusText shouldBe Some("005 - No form bundle found")
    }

    "return None when email is not present in response" in {
      noEmailSubscriptionDisplayResponse.email shouldBe None
    }
  }
}
