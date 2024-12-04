/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.views

import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.must
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, stubMessages}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.ReferrerName
import uk.gov.hmrc.customs.emailfrontend.utils.{FakeIdentifierAgentAction, SpecBase}
import uk.gov.hmrc.customs.emailfrontend.viewmodels.EmailVerifiedOrChangedViewModel
import uk.gov.hmrc.customs.emailfrontend.views.html.email_verified_or_changed

class EmailVerifiedOrChangedViewSpec extends SpecBase {

  "EmailVerifiedOrChangedView" must {

    "render the email verified page correctly with a link" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = testEmail,
        referrerUrl = referrerUrl,
        isVerifyJourney = true,
        appConfig = mockAppConfig
      )

      val html = view(viewModel)
      val content = contentAsString(html)

      content must include(messages(viewModel.titleKey))
      content must include(viewModel.email)
      content must not include(messages("customs.emailfrontend.email-verified.info"))
      content must include(referrerUrl.get)
    }

    "render the email changed page correctly without a link" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = testEmail,
        referrerUrl = None,
        isVerifyJourney = false,
        appConfig = mockAppConfig
      )

      val html = view(viewModel)
      val content = contentAsString(html)

      content must include(messages(viewModel.titleKey))
      content must include(viewModel.email)
      content must include(messages("customs.emailfrontend.email-confirmed.info"))
    }
  }

  trait Setup {
    val app: Application = applicationBuilder[FakeIdentifierAgentAction]().build()
    protected val mockAppConfig: AppConfig = mock[AppConfig]

    val email: String = testEmail
    val referrerUrl: Option[String] = Some("https://finance.example.com")

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest("GET", "/some/resource/path")
    implicit val messages: Messages = stubMessages()

    val view: email_verified_or_changed = app.injector.instanceOf[email_verified_or_changed]

    val tgpUrl = "https://trader-goods.example.com"
    val customsFinanceUrl = "https://finance.example.com"
    val testEmail = "test@example.com"

    when(mockAppConfig.customsFinanceReferrer).thenReturn(Some(ReferrerName("Customs Finance", customsFinanceUrl)))
    when(mockAppConfig.traderGoodsProfilesReferrer).thenReturn(Some(ReferrerName("Trader Goods Profiles", tgpUrl)))
  }
}
