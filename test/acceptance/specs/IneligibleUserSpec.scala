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

package acceptance.specs

import acceptance.wiremockstub._
import common.pages._
import utils.SpecHelper

class IneligibleUserSpec extends AcceptanceTestSpec with SpecHelper with StubAuthClient {

  lazy val randomEoriNumber = "GB" + generateRandomNumberString()
  lazy val randomInternalId = generateRandomNumberString()

  feature("Show ineligible user page for unauthorised users") {

    scenario("A user having no CDS enrolment tries to amend email address") {

      Given("the user has no CDS enrolment")
      authenticateGGUserWithError(randomInternalId, "InsufficientEnrolments")

      When("the user attempts to access the 'What is your email?' page")
      navigateTo(WhatIsYourEmailPageShow)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserPage)

      When("the user accesses the 'Change your email address page")
      navigateTo(ChangeYourEmailAddressPage)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserPage)

      When("the user accesses the 'Check your email address' page")
      navigateTo(CheckYourEmailAddressPage)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserPage)

      When("the user accesses the 'Email confirmed' page")
      navigateTo(EmailConfirmedPage)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserPage)

      When("the user accesses the 'Verify email address' page")
      navigateTo(VerifyYourEmailAddressPage)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserPage)

    }

    scenario("A user with an assistant account tries to amend email") {

      Given("the user has is an assistant on the account")
      authenticate(randomInternalId, randomEoriNumber, "assistant")

      When("the user attempts to access the 'What is your email?' page")
      navigateTo(WhatIsYourEmailPageShow)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserNotAdminPage)
    }

    scenario("A user with an agent account tries to amend email") {

      Given("the user is an agent on the account for an organisation")
      authenticate(randomInternalId, randomEoriNumber, "user","Agent")

      When("the user attempts to access the 'What is your email?' page")
      navigateTo(WhatIsYourEmailPageShow)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserAgentPage)
    }

    scenario("A user with an agent account having no CDS enrolment tries to amend email") {

      Given("the user is an agent on the account for an organisation")
      authenticateGGUserAsAgentWithNoCDSEnrolment(randomInternalId, randomEoriNumber, "user","Agent")

      When("the user attempts to access the 'What is your email?' page")
      navigateTo(WhatIsYourEmailPageShow)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserAgentPage)
    }

    scenario("An assistant user with an organisation account having no CDS enrolment tries to amend email") {

      Given("the user is an agent on the account for an organisation")
      authenticateGGUserAsAgentWithNoCDSEnrolment(randomInternalId, randomEoriNumber, "assistant","Organisation")

      When("the user attempts to access the 'What is your email?' page")
      navigateTo(WhatIsYourEmailPageShow)

      Then("the user should not be allowed")
      verifyCurrentPage(IneligibleUserNotAdminPage)
    }
  }
}
