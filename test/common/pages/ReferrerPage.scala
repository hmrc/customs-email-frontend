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

import utils.{Configuration, SpecHelper}

class ReferrerPage extends BasePage with SpecHelper {

  override val url
    : String = Configuration.frontendHost + "/manage-email-cds/service/"
  override val title = "What is your email address?"

}

object ListedOnReferrerPage extends ReferrerPage {
  override val url
    : String = Configuration.frontendHost + "/manage-email-cds/service/customs-finance"
}

object NotListedOnReferrerPage extends ReferrerPage {
  override val url
    : String = Configuration.frontendHost + "/manage-email-cds/service/xxx"
}
