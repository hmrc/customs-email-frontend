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
import acceptance.utils._
import integration.stubservices.UpdateVerifiedEmailStubService._

class EmailConfirmedSpec extends BaseSpec
  with SpecHelper
  with StubSave4Later
  with StubAuthClient
  with StubEmailVerification
  with StubCustomsDataStore
  with StubSubscriptionDisplay {

  feature("Show Email confirmed to user when the email address is verified") {

    lazy val randomInternalId = generateRandomNumberString()
    lazy val randomEoriNumber = "GB" + generateRandomNumberString()

    scenario("Show email confirmed page without sending email verification link when user email address is verified") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()
      clickOn(StartPage.emailLinkText)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)
      enterText(WhatIsYourEmailPage.emailTextFieldId)("b@a.com")
      clickContinue()

      Then("the user should be on 'Check your email address' page")
      verifyCurrentPage(CheckYourEmailAddressPage)
      assertIsTextVisible(CheckYourEmailAddressPage.emailAddressId)("b@a.com")

      When("the user confirms to update the email address")
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      stubEmailAlreadyVerified()
      stubVerifiedEmailResponse()
      stubEmailUpdatedResponseWithStatus(updatedVerifiedEmailResponse, 200)
      stubCustomsDataStoreOkResponse()
      clickContinue()

      Then("the user should be on 'Email confirmed' page")
      verifyCurrentPage(EmailConfirmedPage)
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedText)("Your email address for CDS has been changed.")
      verifyCustomsDataStoreIsCalled()
      verifyUpdateVerifiedEmailIsCalled(1)
    }

    scenario("Show verify your email page when user does not verify the email and tries to access the 'Email Confirmed' page") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()
      clickOn(StartPage.emailLinkText)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)
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
      verifyEmailVerifiedIsCalled(2)
      verifyCustomsDataStoreIsNotCalled()
      verifyUpdateVerifiedEmailIsCalled(0)

      Then("the user should be on 'Verify email address' page")
      verifyCurrentPage(VerifyYourEmailAddressPage)
      assertIsTextVisible(VerifyYourEmailAddressPage.verifyEmailId)("b@a.com")
    }
  }
}
