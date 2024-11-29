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

import uk.gov.hmrc.customs.emailfrontend.config.AppConfig

case class EmailVerifiedOrChangedViewModel(
                                            email: String,
                                            referrerUrl: Option[String],
                                            isVerifyJourney: Boolean,
                                            appConfig: AppConfig
                                          ) {

  def titleKey: String =
    if (isVerifyJourney)  { "customs.emailfrontend.email-verified.title-and-heading" }
    else { "customs.emailfrontend.email-changed.title-and-heading" }

  def panelKey: String =
    if (isVerifyJourney) { "customs.emailfrontend.email-verified.panel" }
    else { "customs.emailfrontend.email-changed.panel" }

  def messageKey: Option[String] = (hasLink, isVerifyJourney) match {
    case (true, true)  => Some("customs.emailfrontend.email-verified.info")
    case (true, false) => None
    case (false, true) => Some("customs.emailfrontend.email-verified.info")
    case (false, false) => Some("customs.emailfrontend.email-confirmed.info")
  }

  def link: Option[(String, String)] = referrerUrl.flatMap {
    case url if matchesFinanceReferrer(url) =>
      Some("customs.emailfrontend.email.redirect.info.customs-finance.link" -> url)
    case url if matchesTraderGoodsProfilesReferrer(url) =>
      Some("customs.emailfrontend.email.redirect.info.trader-goods-profiles.link" -> url)
    case _ => None
  }

  private def hasLink: Boolean = link.isDefined

  private def matchesFinanceReferrer(url: String): Boolean =
    url == appConfig.customsFinanceReferrer.fold("")(_.continueUrl)

  private def matchesTraderGoodsProfilesReferrer(url: String): Boolean =
    url == appConfig.traderGoodsProfilesReferrer.fold("")(_.continueUrl)
}
