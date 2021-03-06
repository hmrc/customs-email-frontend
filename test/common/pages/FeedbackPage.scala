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

import utils.Configuration

class FeedbackPage extends BasePage {
  override val url: String =
    if (Configuration.frontendHost == "local")
      "localhost:9514/feedback/manage-email-cds"
    else "/feedback/manage-email-cds"
  override val title = "Give feedback - GOV.UK"
}

object FeedbackPage extends FeedbackPage
