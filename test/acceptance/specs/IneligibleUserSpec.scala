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

package acceptance.specs

import acceptance.pages.{IneligibleUserPage, StartPage}
import acceptance.utils._

class IneligibleUserSpec extends BaseSpec with SpecHelper with StubAuthClient {

  feature("Show ineligible user page for unauthorised users") {

    lazy val randomInternalId = generateRandomNumberString()

    scenario("A user having no CDS enrolment tries to amend email address") {

      Given("the user has no CDS enrolment")
      authenticateGGUserWithNoEnrolments(randomInternalId)

      When("the user accesses the start page")
      navigateTo(StartPage)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserPage)
    }
  }
}
