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
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.VerifiedEmailRequest
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.{TEST_LOCAL_DATE_TIME, TEST_REF, testEmail, testEori}
import play.api.libs.json.{JsDefined, JsResultException, JsString, JsSuccess, JsValue, Json}

class UpdateVerifiedEmailRequestSpec extends PlaySpec {

  "UpdateVerifiedEmailRequest" should {
    "parse the model to correct json format" in new Setup {
      val requestJosn: JsValue = Json.toJson[VerifiedEmailRequest](verifiedEmailRequest)

      requestJosn \ "updateVerifiedEmailRequest" \ "requestDetail" \ "emailAddress" shouldBe JsDefined(
        JsString(testEmail)
      )
      requestJosn \ "updateVerifiedEmailRequest" \ "requestDetail" \ "IDNumber"     shouldBe JsDefined(
        JsString(testEori)
      )
      requestJosn \ "updateVerifiedEmailRequest" \ "requestDetail" \ "IDType"       shouldBe JsDefined(JsString("EORI"))
    }
  }

  "UpdateVerifiedEmailRequest.formats" should {

    "generate correct output for Json Reads" in new Setup {
      import UpdateVerifiedEmailRequest.formats

      Json.fromJson(Json.parse(updateVerifiedEmailRequestJsString)) shouldBe JsSuccess(
        updateVerifiedEmailRequestWithResCommonOb
      )
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"verResponse\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[UpdateVerifiedEmailRequest]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(updateVerifiedEmailRequestWithResCommonOb) shouldBe Json.parse(updateVerifiedEmailRequestJsString)
    }
  }

  trait Setup {
    val requestCommon: RequestCommon              = RequestCommon()
    val requestCommonWithDetailsOb: RequestCommon = RequestCommon("CDS", TEST_LOCAL_DATE_TIME, TEST_REF)

    val requestDetail: RequestDetail = RequestDetail(
      IDType = "EORI",
      IDNumber = testEori,
      emailAddress = testEmail,
      emailVerificationTimestamp = DateTimeUtil.dateTime
    )

    val updateVerifiedEmailRequest: UpdateVerifiedEmailRequest =
      UpdateVerifiedEmailRequest(requestCommon, requestDetail)

    val updateVerifiedEmailRequestWithResCommonOb: UpdateVerifiedEmailRequest =
      UpdateVerifiedEmailRequest(
        requestCommonWithDetailsOb,
        requestDetail.copy(emailVerificationTimestamp = TEST_LOCAL_DATE_TIME)
      )

    val verifiedEmailRequest: VerifiedEmailRequest =
      VerifiedEmailRequest(updateVerifiedEmailRequest = updateVerifiedEmailRequest)

    val updateVerifiedEmailRequestJsString: String =
      """{"requestCommon":{
        |"regime":"CDS",
        |"receiptDate":"2024-12-15T14:30:28Z",
        |"acknowledgementReference":"12345acnd677"},
        |"requestDetail":
        |{"IDType":"EORI",
        |"IDNumber":"test_eori",
        |"emailAddress":"test@example.com",
        |"emailVerificationTimestamp":"2024-12-15T14:30:28Z"
        |}}""".stripMargin
  }
}
