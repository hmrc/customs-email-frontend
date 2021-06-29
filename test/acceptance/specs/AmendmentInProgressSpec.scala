/*
 * Copyright 2021 HM Revenue & Customs
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

import acceptance.wiremockstub.{StubAuthClient, StubEmailVerification, StubSave4Later, StubSubscriptionDisplay}
import common.pages.{ChangeYourEmailAddressPage, StartPage, YouCannotChangeYourEmailAddressPage}
import utils.SpecHelper

class AmendmentInProgressSpec
    extends AcceptanceTestSpec with StubSave4Later with StubAuthClient with SpecHelper with StubEmailVerification with StubSubscriptionDisplay {

  Feature("Amendment already in progress") {

    lazy val randomInternalId = generateRandomNumberString()
    lazy val randomEoriNumber = "GB" + generateRandomNumberString()

    Scenario(
      "User returning to the service within 2 hours after successfully amending the email") {

      Given("the user has successfully amended the email")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithData(randomInternalId)(emailDetailsWithTimestamp)

      When("the user returns to amend the email again within 2 hours")
      navigateTo(StartPage)
      addUserInSession()
      verifyCurrentPage(StartPage)
      clickOn(StartPage.startNowButton)

      Then("the user should be redirected to 'You cannot change your email address' page")
      verifyCurrentPage(YouCannotChangeYourEmailAddressPage)
    }

    Scenario("User returning to the service after 2 hours of successfully amending the email") {

      Given("the user has successfully amended the email")
      authenticate(randomInternalId, randomEoriNumber)
      save4LaterWithData(randomInternalId)(emailDetailsWithTimestampOver2Hours)
      stubSubscriptionDisplayOkResponse(randomEoriNumber)
      stubVerifiedEmailResponse()

      When("the user returns to amend the email again after 2 hours")
      navigateTo(StartPage)
      addUserInSession()
      verifyCurrentPage(StartPage)
      clickOn(StartPage.startNowButton)

      Then("the user should be on 'Enter a new email address' page")
      verifyCurrentPage(ChangeYourEmailAddressPage)
    }
  }
}
