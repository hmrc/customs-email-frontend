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

import org.joda.time.format.ISODateTimeFormat
import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsDefined, JsString, Json}
import uk.gov.hmrc.customs.emailfrontend.model._
class UpdateVerifielEmailRequestSpec extends PlaySpec {


  val verifiedEmailRequestJson =
    Json.parse("""{
                 |  "updateverifiedemailRequest": {
                 |    "requestCommon": {
                 |      "regime": "CDS",
                 |      "receiptDate": "2019-08-22T13:55:55Z",
                 |      "acknowledgementReference": "16061ef4ea8740128ac49e9787d3d1f3"
                 |    },
                 |    "requestDetail": {
                 |      "IDType": "EORI",
                 |      "IDNumber": "GB173822879792263",
                 |      "emailAddress": "mickey.mouse@disneyland.com",
                 |      "emailVerificationTimestamp": "2019-08-22T13:55:55Z"
                 |    }
                 |  }
                 |}""".stripMargin
    ).as[VerifiedEmailRequest]


  val requestCommon =  RequestCommon()
  val requestDetail = RequestDetail(IDType = "EORI",
    IDNumber = "GBXXXXXXXXXXXX",
    emailAddress = "mickey.mouse@disneyland.com",
    emailVerificationTimestamp = MDGDateFormat.dateFormat.toString(ISODateTimeFormat.dateTimeNoMillis().withZoneUTC()))
  val updateVerifiedEmailRequest = UpdateVerifiedEmailRequest(requestCommon,requestDetail)
  val verifiedEmailRequest = VerifiedEmailRequest(updateverifiedemailRequest=updateVerifiedEmailRequest)
  "UpdateVerifiedEmailRequest" should {

    "parse the model to correct json format" in {
      val requestJosn = Json.toJson[VerifiedEmailRequest](verifiedEmailRequest)
      requestJosn \ "updateverifiedemailRequest" \ "requestDetail" \ "emailAddress"  shouldBe JsDefined(JsString("mickey.mouse@disneyland.com"))
      requestJosn \ "updateverifiedemailRequest" \ "requestDetail" \ "IDNumber"  shouldBe JsDefined(JsString("GBXXXXXXXXXXXX"))
      requestJosn \ "updateverifiedemailRequest" \ "requestDetail" \ "IDType"  shouldBe JsDefined(JsString("EORI"))
    }

  }
}
