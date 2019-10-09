/*
 * Copyright 2019 HM Revenue & Customs
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

package unit.model

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import uk.gov.hmrc.customs.emailfrontend.model.SubscriptionDisplayResponse

class SubscriptionDisplayResponseSpec extends PlaySpec {

  private val subscriptionDisplayResponse =
    Json.parse("""{
                 |  "subscriptionDisplayResponse": {
                 |    "responseDetail": {
                 |      "contactInformation": {
                 |        "emailAddress": "test@email.com",
                 |        "emailVerificationTimestamp": "2019-09-06T12:30:59Z"
                 |      }
                 |    }
                 |  }
                 |}""".stripMargin
    ).as[SubscriptionDisplayResponse]

  private val noFormBundleSubscriptionDisplayResponse =
    Json.parse("""{
                 |  "subscriptionDisplayResponse": {
                 |    "responseCommon":{
                 |      "statusText": "005 - No form bundle found",
                 |      "processingDate": "2016-08-17T19:33:47Z"
                 |     }
                 |    }
                 |}""".stripMargin
    ).as[SubscriptionDisplayResponse]

  private val noEmailSubscriptionDisplayResponse =
    Json.parse("""{
                 |  "subscriptionDisplayResponse": {
                 |    "responseDetail": {
                 |      "contactInformation": {
                 |        "emailVerificationTimestamp": "2019-09-06T12:30:59Z"
                 |      }
                 |    }
                 |  }
                 |}""".stripMargin
    ).as[SubscriptionDisplayResponse]


  "SubscriptionDisplayResponse Object" should {

    "contain email when present in response" in {
      subscriptionDisplayResponse.email shouldBe Some("test@email.com")
    }

    "contain status when present in response" in {
      noFormBundleSubscriptionDisplayResponse.status shouldBe Some("005 - No form bundle found")
    }

    "return None when email is not present in response" in {
      noEmailSubscriptionDisplayResponse.email shouldBe None
    }
  }
}
