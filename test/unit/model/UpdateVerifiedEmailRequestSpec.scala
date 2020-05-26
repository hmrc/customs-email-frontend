/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json._
import uk.gov.hmrc.customs.emailfrontend.DateTimeUtil
import uk.gov.hmrc.customs.emailfrontend.connectors.http.responses.VerifiedEmailRequest
import uk.gov.hmrc.customs.emailfrontend.model._

class UpdateVerifiedEmailRequestSpec extends PlaySpec {

  val requestCommon = RequestCommon()
  val requestDetail = RequestDetail(
    IDType = "EORI",
    IDNumber = "GBXXXXXXXXXXXX",
    emailAddress = "test@email.com",
    emailVerificationTimestamp = DateTimeUtil.dateTime
  )
  val updateVerifiedEmailRequest =
    UpdateVerifiedEmailRequest(requestCommon, requestDetail)
  val verifiedEmailRequest = VerifiedEmailRequest(
    updateVerifiedEmailRequest = updateVerifiedEmailRequest)

  "UpdateVerifiedEmailRequest" should {
    "parse the model to correct json format" in {
      val requestJosn = Json.toJson[VerifiedEmailRequest](verifiedEmailRequest)
      requestJosn \ "updateVerifiedEmailRequest" \ "requestDetail" \ "emailAddress" shouldBe JsDefined(
        JsString("test@email.com")
      )
      requestJosn \ "updateVerifiedEmailRequest" \ "requestDetail" \ "IDNumber" shouldBe JsDefined(
        JsString("GBXXXXXXXXXXXX")
      )
      requestJosn \ "updateVerifiedEmailRequest" \ "requestDetail" \ "IDType" shouldBe JsDefined(
        JsString("EORI"))
    }
  }
}
