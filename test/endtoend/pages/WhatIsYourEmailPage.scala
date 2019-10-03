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

package endtoend.pages

import endtoend.utils.Configuration
import org.openqa.selenium.By

abstract class EmailPage extends BasePage {
  val emailTextFieldId: By = By.id("email")
}

object WhatIsYourEmailPage extends EmailPage {
  override val url: String = Configuration.frontendHost + "/manage-email-cds/email-address/verify-email-address"
  override val title = "What is your email address?"
}

object ChangeYourEmailAddressPage extends EmailPage {
  override val url: String = Configuration.frontendHost + "/manage-email-cds/change-email-address/create"
  override val title = "Change your email address for CDS"
}
