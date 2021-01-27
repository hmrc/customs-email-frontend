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

package common.pages

import org.openqa.selenium.By
import utils.Configuration

class CheckYourEmailAddressPage extends BasePage {
  override val url
    : String = Configuration.frontendHost + "/manage-email-cds/check-email-address"
  override val title = "Check your email address"

  val emailAddressId: By = By.id("cya-answer-id")
  val yesEmailAddressCss: By = By.cssSelector("#isYes-true")
  val noEmailAddressCss: By = By.cssSelector("#isYes-false")
}

object CheckYourEmailAddressPage extends CheckYourEmailAddressPage
