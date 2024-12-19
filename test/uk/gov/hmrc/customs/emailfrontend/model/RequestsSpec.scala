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

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.POST
import uk.gov.hmrc.customs.emailfrontend.controllers.routes
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import org.scalatest.matchers.must.Matchers.mustBe

class RequestsSpec extends SpecBase {
  "user" should {
    "return the logged in user" in new Setup {
      authenticReq.user mustBe user
      eorirequest.user mustBe user
    }
  }

  trait Setup {
    val currentEmail = "test_current_mail@test.com"
    val newMail      = "test_new_mail@test.com"
    val eori         = "test_eori"

    val fakeReq: FakeRequest[EmailDetails]           = FakeRequest().withBody(EmailDetails(Some(currentEmail), newMail, None))
    val user: LoggedInUser                           = LoggedInUser(InternalId("some_id"), None, None, eori)
    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(POST, routes.ChangingYourEmailController.submit.url)

    val authenticReq = new AuthenticatedRequest[EmailDetails](request = fakeReq, user)
    val eorirequest  = new EoriRequest[EmailDetails](authenticReq, Eori(eori))
  }
}
