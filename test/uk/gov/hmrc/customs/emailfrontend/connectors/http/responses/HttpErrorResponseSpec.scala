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

package uk.gov.hmrc.customs.emailfrontend.connectors.http.responses

import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import play.api.libs.json.{JsResultException, JsSuccess, Json}
import uk.gov.hmrc.customs.emailfrontend.model.{
  MessagingServiceParam, RequestCommon, RequestDetail, ResponseCommon, UpdateVerifiedEmailRequest,
  UpdateVerifiedEmailResponse
}
import uk.gov.hmrc.customs.emailfrontend.utils.TestData.{
  TEST_LOCAL_DATE_TIME, TEST_REF, dateFormatter01, testEmail, testUtcTimestamp
}
import uk.gov.hmrc.customs.emailfrontend.model.MessagingServiceParam.formBundleIdParamName

import java.time.LocalDateTime

class HttpErrorResponseSpec extends SpecBase {

  "VerifiedEmailRequest.formats" should {

    "generate correct output for Json Reads" in new Setup {
      import VerifiedEmailRequest.formats

      Json.fromJson(Json.parse(verifiedEmailRequestObJsString)) shouldBe JsSuccess(verifiedEmailRequestOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"reqCom\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[VerifiedEmailRequest]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(verifiedEmailRequestOb) shouldBe Json.parse(verifiedEmailRequestObJsString)
    }
  }

  "VerifiedEmailResponse.format" should {

    "generate correct output for Json Reads" in new Setup {
      import VerifiedEmailResponse.format

      Json.fromJson(Json.parse(verifiedEmailResponseObJsString)) shouldBe JsSuccess(verifiedEmailResponseOb)
    }

    "throw exception for invalid Json" in {
      val invalidJson = "{ \"verResponse\": \"pending\" }"

      intercept[JsResultException] {
        Json.parse(invalidJson).as[VerifiedEmailResponse]
      }
    }

    "generate correct output for Json Writes" in new Setup {
      Json.toJson(verifiedEmailResponseOb) shouldBe Json.parse(verifiedEmailResponseObJsString)
    }
  }

  trait Setup {
    val requestCommonOb: RequestCommon = RequestCommon("CDS", TEST_LOCAL_DATE_TIME, TEST_REF)
    val reqDetail: RequestDetail       = RequestDetail("idType", "idNumber", testEmail, TEST_LOCAL_DATE_TIME)

    val updateVerifiedEmailRequest: UpdateVerifiedEmailRequest =
      UpdateVerifiedEmailRequest(requestCommonOb, reqDetail)

    val verifiedEmailRequestOb: VerifiedEmailRequest = VerifiedEmailRequest(updateVerifiedEmailRequest)

    val verifiedEmailRequestObJsString: String =
      """{"updateVerifiedEmailRequest":{
        |"requestCommon":{
        |"regime":"CDS","receiptDate":"2024-12-15T14:30:28Z","acknowledgementReference":"12345acnd677"},
        |"requestDetail":{
        |"IDType":"idType",
        |"IDNumber":"idNumber",
        |"emailAddress":"test@example.com",
        |"emailVerificationTimestamp":"2024-12-15T14:30:28Z"
        |}
        |}}""".stripMargin

    val status                     = "OK"
    val statusText: Option[String] = Some("200 - OK")
    val validDateTime: String      = testUtcTimestamp
    val invalidDateTime            = "2024-03-11T14:30:00.123456789Z"

    val validDateTimeObj: LocalDateTime           = LocalDateTime.parse(validDateTime, dateFormatter01)
    val returnParams: List[MessagingServiceParam] = List(MessagingServiceParam(formBundleIdParamName, "testValue"))
    val responeCommonOb: ResponseCommon           = ResponseCommon(status, statusText, validDateTimeObj, returnParams)

    val updateVerifiedEmailResponse: UpdateVerifiedEmailResponse = UpdateVerifiedEmailResponse(responeCommonOb)

    val verifiedEmailResponseOb: VerifiedEmailResponse = VerifiedEmailResponse(updateVerifiedEmailResponse)

    val verifiedEmailResponseObJsString: String =
      """{
        |"updateVerifiedEmailResponse":
        |{"responseCommon":
        |{"status":"OK",
        |"statusText":"200 - OK",
        |"processingDate":"2021-01-01T11:11:11Z",
        |"returnParameters":[{"paramName":"ETMPFORMBUNDLENUMBER","paramValue":"testValue"}]
        |}}}""".stripMargin
  }
}
