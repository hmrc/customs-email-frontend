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

import acceptance.pages.{CheckYourEmailAddressPage, WhatIsYourEmailPage}
import acceptance.utils.SpecHelper

class ChangeEmailSpec extends BaseSpec with SpecHelper {

  feature("change email address") {
    scenario("user changes the email address") {
      Given("the user has successfully logged in")
        authenticate()
        save4LaterWithNoData()
        navigateTo(WhatIsYourEmailPage)
        verifyCurrentPage(WhatIsYourEmailPage)
      When("the user provides an email address to change")
        save4LaterWithData()
        enterText(WhatIsYourEmailPage.emailTextFieldId)("b@a.com")
        clickContinue()
      Then("the user should be on 'Check your email address' page")
        verifyCurrentPage(CheckYourEmailAddressPage)
      Then("the new email address provided should be updated")
        assertIsTextVisible(CheckYourEmailAddressPage.emailAddressId)("b@a.com")
    }
  }
}
