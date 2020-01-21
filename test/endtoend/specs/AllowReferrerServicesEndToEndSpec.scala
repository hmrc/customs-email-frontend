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

package endtoend.specs

import common.pages.{AuthLoginStubPage, ChangeYourEmailAddressPage, CheckYourEmailAddressPage, EmailConfirmedPage, ListedOnReferrerPage, NotListedOnReferrerPage, ReferrerPage, StartPage, WhatIsYourEmailPage}
import utils.SpecHelper

class AllowReferrerServicesEndToEndSpec extends EndToEndTestSpec with SpecHelper {

  feature("Allow users to verify email address when they are available in the referrer list") {


    scenario("User should be allowed to verify email address when redirected from 'customs-finance'") {
      Given("the user is successfully logged in 'customs-finance' service and the email address is not verified")
      navigateTo(AuthLoginStubPage)
      AuthLoginStubPage.login(generateRandomNumberString(), eori = "GB123123123")
      verifyCurrentPage(StartPage)

      When("the user gets redirected to 'manage-email-cds' service for email verification")
      navigateTo(ListedOnReferrerPage)
      verifyCurrentPage(WhatIsYourEmailPage)

      Then("the user should be able to successfully verify the email")
      enterText(WhatIsYourEmailPage.emailTextFieldId)("new-john.doe@example.com")
      clickContinue()
      verifyCurrentPage(CheckYourEmailAddressPage)
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      clickContinue()
      verifyCurrentPage(EmailConfirmedPage)
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedTextPartOne)("Your email address new-john.doe@example.com will be active in 2 hours.")
      clickOn(EmailConfirmedPage.signOutId)

    }

    scenario("User should be allowed to verify email address when redirected from a service which is not on the referrer list") {
      Given("the user is successfully logged in 'an non referrer' service and the email address is not verified")
      navigateTo(AuthLoginStubPage)
      AuthLoginStubPage.login(generateRandomNumberString(), eori = "GB123123123")
      verifyCurrentPage(StartPage)

      When("the user gets redirected to 'manage-email-cds' service for email verification")
      navigateTo(NotListedOnReferrerPage)
      verifyCurrentPage(WhatIsYourEmailPage)

      Then("the user should be able to successfully verify the email")
      enterText(WhatIsYourEmailPage.emailTextFieldId)("new-john.doe@example.com")
      clickContinue()
      verifyCurrentPage(CheckYourEmailAddressPage)
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      clickContinue()
      verifyCurrentPage(EmailConfirmedPage)

      //TODO : the assertion below will change when the content gets dynamic
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedTextPartOne)("Your email address new-john.doe@example.com will be active in 2 hours.")
      clickOn(EmailConfirmedPage.signOutId)
    }
  }
}
