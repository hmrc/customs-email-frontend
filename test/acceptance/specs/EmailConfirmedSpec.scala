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

import common.pages._
import acceptance.wiremockstub._
import utils.SpecHelper

class EmailConfirmedSpec extends AcceptanceTestSpec
  with SpecHelper
  with StubSave4Later
  with StubAuthClient
  with StubEmailVerification
  with StubCustomsDataStore
  with StubSubscriptionDisplay
  with StubUpdateVerifiedEmail {

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
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)(emailDetails)
      verifyCurrentPage(ChangeYourEmailAddressPage)
      enterText(ChangeYourEmailAddressPage.emailTextFieldId)("b@a.com")
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
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedTextPartOne)("Your new email address will be active in 24 hours.")
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedTextPartTwo)("Until then we will send CDS emails to the email address you were using previously.")
      verifyEmailVerifiedIsCalled(2)
      verifyCustomsDataStoreIsCalled(1)
      verifyUpdateVerifiedEmailIsCalled(1)
    }

    scenario("Show 'Email confirmed' page when user returns to the service after verifying the email address but could not successfully update the email address") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)(emailDetails)
      verifyCurrentPage(ChangeYourEmailAddressPage)
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

      When("the user returns to the service after confirming the email address but was unsuccessful to update")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithData(randomInternalId)(emailDetails)
      stubVerifiedEmailResponse()
      stubEmailUpdatedResponseWithStatus(updatedVerifiedEmailResponse, 200)
      stubCustomsDataStoreOkResponse()
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      clickOn(StartPage.startNowButton)

      Then("the user should be on 'Email confirmed' page upon successfully updating the email")
      verifyCurrentPage(EmailConfirmedPage)
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedTextPartOne)("Your new email address will be active in 24 hours.")
      assertIsTextVisible(EmailConfirmedPage.verifyEmailConfirmedTextPartTwo)("Until then we will send CDS emails to the email address you were using previously.")
      verifyCustomsDataStoreIsCalled(1)
      verifyUpdateVerifiedEmailIsCalled(1)
    }

    scenario("Show 'Check your email' page when user returns to the service without verifying the email address") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)(emailDetails)
      verifyCurrentPage(ChangeYourEmailAddressPage)
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

      When("the user returns to the service without confirming the email address")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithData(randomInternalId)(emailDetails)
      stubNotVerifiedEmailResponse()
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      clickOn(StartPage.startNowButton)

      Then("the user should be on 'Check your email address' page")
      verifyCurrentPage(CheckYourEmailAddressPage)
      assertIsTextVisible(CheckYourEmailAddressPage.emailAddressId)("b@a.com")
      verifyCustomsDataStoreIsCalled(0)
      verifyUpdateVerifiedEmailIsCalled(0)
      verifyEmailVerifiedIsCalled(2)
    }

    scenario("Show verify your email page when user does not verify the email and tries to access the 'Email Confirmed' page") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)(emailDetails)
      verifyCurrentPage(ChangeYourEmailAddressPage)
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
