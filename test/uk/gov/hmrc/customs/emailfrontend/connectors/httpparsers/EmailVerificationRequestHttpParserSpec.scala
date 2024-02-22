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

package uk.gov.hmrc.customs.emailfrontend.connectors.httpparsers

import play.api.http.Status
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString

class EmailVerificationRequestHttpParserSpec extends SpecBase {

  "CreateEmailVerificationRequestHttpReads" should {
    "successfully parse a CREATED response" in new Setup {
      val httpResponse = HttpResponse(Status.CREATED, emptyString)
      val result = httpParser.read("POST", "/some/url", httpResponse)

      result shouldBe Right(EmailVerificationRequestHttpParser.EmailVerificationRequestSent)
    }

    "successfully parse a CONFLICT response" in new Setup {
      val httpResponse = HttpResponse(Status.CONFLICT, emptyString)
      val result = httpParser.read("POST", "/some/url", httpResponse)

      result shouldBe Right(EmailVerificationRequestHttpParser.EmailAlreadyVerified)
    }

    "successfully parse a BAD_REQUEST response" in new Setup {
      val httpResponse = HttpResponse(Status.BAD_REQUEST, "Invalid request")
      val result = httpParser.read("POST", "/some/url", httpResponse)

      result shouldBe Left(
        EmailVerificationRequestHttpParser.EmailVerificationRequestFailure(
          Status.BAD_REQUEST, "Invalid request"))
    }
  }

  trait Setup {
    val httpParser = EmailVerificationRequestHttpParser.CreateEmailVerificationRequestHttpReads
  }
}
