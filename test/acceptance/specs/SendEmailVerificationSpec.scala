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

import common.pages.{CheckYourEmailAddressPage, StartPage, VerifyYourEmailAddressPage, WhatIsYourEmailPage}
import acceptance.wiremockstub._
import utils.SpecHelper

class SendEmailVerificationSpec extends AcceptanceTestSpec
  with SpecHelper
  with StubAuthClient
  with StubSave4Later
  with StubEmailVerification
  with StubSubscriptionDisplay {

  val randomInternalId = generateRandomNumberString()
  val randomEoriNumber = "GB" + generateRandomNumberString()

  feature("Send email to user for verification") {

    scenario("organisation user amends the email and submits for verification") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      addUserInSession()
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)(emailDetails)
      enterText(WhatIsYourEmailPage.emailTextFieldId)("b@a.com")
      clickContinue()

      Then("the user should be on 'Check your email address' page")
      verifyCurrentPage(CheckYourEmailAddressPage)
      assertIsTextVisible(CheckYourEmailAddressPage.emailAddressId)("b@a.com")
      stubVerificationRequestSent()
      stubNotVerifiedEmailResponse()

      When("the user confirms to update the email address")
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      clickContinue()

      Then("the user should be on 'Verify email address' page")
      verifyCurrentPage(VerifyYourEmailAddressPage)
      assertIsTextVisible(VerifyYourEmailAddressPage.verifyEmailId)("b@a.com")
    }

    scenario("individual user amends the email and submits for verification") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber, affinityGroup = "Individual")
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      addUserInSession()
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      When("the user provides an email address to change")
      save4LaterWithData(randomInternalId)(emailDetails)
      enterText(WhatIsYourEmailPage.emailTextFieldId)("b@a.com")
      clickContinue()

      Then("the user should be on 'Check your email address' page")
      verifyCurrentPage(CheckYourEmailAddressPage)
      assertIsTextVisible(CheckYourEmailAddressPage.emailAddressId)("b@a.com")
      stubVerificationRequestSent()
      stubNotVerifiedEmailResponse()

      When("the user confirms to update the email address")
      clickOn(CheckYourEmailAddressPage.yesEmailAddressCss)
      clickContinue()

      Then("the user should be on 'Verify email address' page")
      verifyCurrentPage(VerifyYourEmailAddressPage)
      assertIsTextVisible(VerifyYourEmailAddressPage.verifyEmailId)("b@a.com")
    }
  }
}
