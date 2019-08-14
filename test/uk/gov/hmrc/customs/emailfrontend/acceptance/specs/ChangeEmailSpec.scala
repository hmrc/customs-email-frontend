package uk.gov.hmrc.customs.emailfrontend.acceptance.specs

import uk.gov.hmrc.customs.emailfrontend.acceptance.pages.WhatIsYourEmailPage

class ChangeEmailSpec extends BaseSpec {

  feature("change email address") {
    scenario("user changes the email address") {
      Given("the user has successfully logged in")
        WhatIsYourEmailPage.navigate()
      When("the user provides an email address to change")
      Then("the new email address provided should be updated")
    }
  }

}
