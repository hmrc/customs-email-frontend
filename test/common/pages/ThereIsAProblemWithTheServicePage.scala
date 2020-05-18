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

package common.pages

import utils.Configuration

abstract class ThereIsAProblemWithTheServicePage extends BasePage {
  override val title = "Sorry, there is a problem with the service"
}

object FetchEmailThereIsAProblemWithTheServicePage extends ThereIsAProblemWithTheServicePage {
  override val url
    : String = Configuration.frontendHost + "/manage-email-cds/change-email-address/problem-with-this-service"
}

object EmailNotSavedThereIsAProblemWithTheServicePage extends ThereIsAProblemWithTheServicePage {
  override val url
    : String = Configuration.frontendHost + "/manage-email-cds/email-address-confirmed/problem-with-this-service"
}
