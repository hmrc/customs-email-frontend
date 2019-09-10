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

import acceptance.pages._
import acceptance.utils.SpecHelper

class EmailConfirmedSpec extends BaseSpec with SpecHelper {


  feature("Show Email confirmed to user when the email address is verified") {
    scenario("Show email confirmed page without sending email verification link when user email address is verified") {

      Given("the user has successfully logged in")
      authenticate()
      save4LaterWithNoData()
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      authenticateGGUserAndReturnEoriEnrolment()
      clickOn(StartPage.emailLinkText)

      When("the user provides an email address to change")
      save4LaterWithData()
      enterText(WhatIsYourEmailPage.emailTextFieldId)("b@a.com")
      clickContinue()

      Then("the user should be on 'Check your email address' page")
      verifyCurrentPage(CheckYourEmailAddressPage)
      assertIsTextVisible(CheckYourEmailAddressPage.emailAddressId)("b@a.com")

      When("the user confirms to update the email address")
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      stubEmailAlreadyVerified()
      stubVerifiedEmailResponse()
      stubCustomsDataStoreOkResponse()
      clickContinue()

      Then("the user should be on 'Email confirmed' page")
      verifyCurrentPage(EmailConfirmedPage)
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedText)("Your email address for CDS has been changed.")
      verifyCustomsDataStoreIsCalled()
    }

    scenario("Show verify your email page when user does not verify the email and tries to access the 'Email Confirmed' page") {

      Given("the user has successfully logged in")
      authenticate()
      save4LaterWithNoData()
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      authenticateGGUserAndReturnEoriEnrolment()
      clickOn(StartPage.emailLinkText)

      When("the user provides an email address to change")
      save4LaterWithData()
      enterText(WhatIsYourEmailPage.emailTextFieldId)("b@a.com")
      clickContinue()

      Then("the user should be on 'Check your email address' page")
      verifyCurrentPage(CheckYourEmailAddressPage)
      assertIsTextVisible(CheckYourEmailAddressPage.emailAddressId)("b@a.com")

      When("the user confirms to update the email address")
      stubVerificationRequestSent()
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      clickContinue()


      Then("the user should be on 'Verify email address' page")
      verifyCurrentPage(VerifyYourEmailAddressPage)
      assertIsTextVisible(VerifyYourEmailAddressPage.verifyEmailId)("b@a.com")
      stubNotVerifiedEmailResponse()

      When("the user attempts to access 'Email confirmed' page")
      navigateTo(EmailConfirmedPage)
      verifyEmailVerifiedIsCalled()
      verifyCustomsDataStoreIsNotCalled()

      Then("the user should be on 'Verify email address' page")
      verifyCurrentPage(VerifyYourEmailAddressPage)
      assertIsTextVisible(VerifyYourEmailAddressPage.verifyEmailId)("b@a.com")
    }

  }

}
