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

import org.joda.time.DateTime
import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.customs.emailfrontend.model._

class RequestDetailsSpec extends PlaySpec {

  val dateTime =  new DateTime ("2019-10-23T07:55:54Z")
  val requestDetail = RequestDetail(IDType = "EORI",
    IDNumber = "GBXXXXXXXXXXXX",
    emailAddress = "test@email.com",
    emailVerificationTimestamp = dateTime)
    val expectedMap = Map(
      "idType" -> "EORI",
      "idNumber" -> "GBXXXXXXXXXXXX",
      "email" -> "test@email.com",
      "emailVerificationTimestamp" -> "2019-10-23T07:55:54Z")

  "RequestDetail" should {
    "parse the model to Audit event format Map" in {
      requestDetail.toAuditMap shouldBe expectedMap
    }
  }
}
