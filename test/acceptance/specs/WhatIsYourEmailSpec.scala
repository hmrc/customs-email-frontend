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

class WhatIsYourEmailSpec extends AcceptanceTestSpec
  with SpecHelper
  with StubSave4Later
  with StubAuthClient
  with StubEmailVerification
  with StubCustomsDataStore
  with StubSubscriptionDisplay {

  feature("Show 'What is your email' page when the email address is not verified") {

    lazy val randomInternalId = generateRandomNumberString()
    lazy val randomEoriNumber = "GB" + generateRandomNumberString()

    scenario("Show 'What is your email' page when email is not verified") {

      Given("the user has successfully logged in")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)

      When("the user email is not verified")
      stubSubscriptionWithoutTimestampDisplayOkResponse(randomEoriNumber)
      stubNotVerifiedEmailResponse()
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      Then("the user should be on 'What is your email' page")
      verifyCurrentPage(WhatIsYourEmailPage)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)
    }

    scenario("Show 'What is your email' page when email is not available") {
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithNoData(randomInternalId)
      navigateTo(StartPage)
      verifyCurrentPage(StartPage)
      stubSubscriptionDisplayOk200EmailNotAvailableResponse(randomEoriNumber)

      When("the user click on 'Start now' button")
      clickOn(StartPage.startNowButton)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)

      Then("the user should be on 'What is your email' page")
      verifyCurrentPage(WhatIsYourEmailPage)
      verifySubscriptionDisplayIsCalled(1, randomEoriNumber)
    }
  }
}
