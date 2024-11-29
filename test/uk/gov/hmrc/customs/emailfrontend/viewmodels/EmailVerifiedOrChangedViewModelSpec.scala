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

package uk.gov.hmrc.customs.emailfrontend.viewmodels

import org.mockito.Mockito.when
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.i18n.Messages
import play.api.test.Helpers
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.model.ReferrerName
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase

class EmailVerifiedOrChangedViewModelSpec extends SpecBase {

  "EmailVerifiedOrChangedViewModel" should {

    "return correct titleKey for verify journey" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = "test@example.com",
        referrerUrl = None,
        isVerifyJourney = true,
        appConfig = appConfig
      )

      viewModel.titleKey mustBe "customs.emailfrontend.email-verified.title-and-heading"
    }

    "return correct titleKey for change journey" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = "test@example.com",
        referrerUrl = None,
        isVerifyJourney = false,
        appConfig = appConfig
      )

      viewModel.titleKey mustBe "customs.emailfrontend.email-changed.title-and-heading"
    }

    "return correct messageKey and link for verify journey with finance referrer" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = "test@example.com",
        referrerUrl = Some(customsFinanceUrl),
        isVerifyJourney = true,
        appConfig = appConfig
      )

      viewModel.messageKey mustBe Some("customs.emailfrontend.email-verified.info")
      viewModel.link mustBe Some("customs.emailfrontend.email.redirect.info.customs-finance.link" -> customsFinanceUrl)
    }

    "return correct messageKey and link for change journey with trader goods profiles referrer" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = "test@example.com",
        referrerUrl = Some(tgpUrl),
        isVerifyJourney = false,
        appConfig = appConfig
      )

      viewModel.messageKey mustBe None
      viewModel.link mustBe Some("customs.emailfrontend.email.redirect.info.trader-goods-profiles.link" -> tgpUrl)
    }

    "return default messageKey for verify journey with no matching referrer" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = "test@example.com",
        referrerUrl = Some("https://unknown.example.com"),
        isVerifyJourney = true,
        appConfig = appConfig
      )

      viewModel.messageKey mustBe Some("customs.emailfrontend.email-verified.info")
      viewModel.link mustBe None
    }

    "return default messageKey for change journey with no matching referrer" in new Setup {
      val viewModel = EmailVerifiedOrChangedViewModel(
        email = "test@example.com",
        referrerUrl = Some("https://unknown.example.com"),
        isVerifyJourney = false,
        appConfig = appConfig
      )

      viewModel.messageKey mustBe Some("customs.emailfrontend.email-confirmed.info")
      viewModel.link mustBe None
    }
  }

  trait Setup {
    implicit val messages: Messages = Helpers.stubMessages()
    val appConfig: AppConfig = mock[AppConfig]

    val tgpUrl = "https://trader-goods.example.com"
    val customsFinanceUrl = "https://finance.example.com"

    when(appConfig.customsFinanceReferrer).thenReturn(Some(ReferrerName("Customs Finance", customsFinanceUrl)))
    when(appConfig.traderGoodsProfilesReferrer).thenReturn(Some(ReferrerName("Trader Goods Profiles", tgpUrl)))
  }
}
