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

package endtoend

import acceptance.pages._
import acceptance.specs.EndToEndTestSpec
import acceptance.utils.SpecHelper

class VerifyEmailAddressEndToEndSpec extends EndToEndTestSpec with SpecHelper{

  feature("Amend email address") {

    lazy val credId = generateRandomNumberString()

    scenario("User returns to the service to amend an email address within 24 hours and should be redirected to 'You cannot change' page"){
      Given("the user has successfully changed the email address")

      navigateTo(AuthLoginStubPage)
      AuthLoginStubPage.login(credId)
      verifyCurrentPage(StartPage)
      clickOn(StartPage.emailLinkText)
      verifyCurrentPage(ChangeYourEmailAddressPage)
      enterText(WhatIsYourEmailPage.emailTextFieldId)("b@a.com")
      clickContinue()
      verifyCurrentPage(CheckYourEmailAddressPage)
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      clickContinue()
      verifyCurrentPage(EmailConfirmedPage)
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedText)("Your email address for CDS has been changed.")
      clickOn(EmailConfirmedPage.signOutId)

      When("the user logs in to amend the email")
      navigateTo(AuthLoginStubPage)
      AuthLoginStubPage.login(credId)
      verifyCurrentPage(StartPage)
      clickOn(StartPage.emailLinkText)

      Then("the user should be on 'You cannot use this service' page")
      verifyCurrentPage(YouCannotChangeYourEmailAddressPage)
      navigateTo(ChangeYourEmailAddressPage)
      verifyCurrentPage(YouCannotChangeYourEmailAddressPage)
      navigateTo(CheckYourEmailAddressPage)
      verifyCurrentPage(YouCannotChangeYourEmailAddressPage)
      navigateTo(EmailConfirmedPage)
      verifyCurrentPage(YouCannotChangeYourEmailAddressPage)
      navigateTo(VerifyYourEmailAddressPage)
      verifyCurrentPage(YouCannotChangeYourEmailAddressPage)
      navigateTo(WhatIsYourEmailPage)
//      verifyCurrentPage(YouCannotChangeYourEmailAddressPage)

    }
  }

}
