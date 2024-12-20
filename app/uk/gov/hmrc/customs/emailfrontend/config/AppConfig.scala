/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.config

import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig.configLoader
import uk.gov.hmrc.customs.emailfrontend.model.ReferrerName
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}
import scala.jdk.CollectionConverters.*

@Singleton
class AppConfig @Inject() (val config: Configuration, servicesConfig: ServicesConfig) {

  lazy val accessibilityLinkUrl: String = config.get[String]("external-url.accessibility-statement")
  val ggSignInRedirectUrl: String       = config.get[String]("external-url.company-auth-frontend.continue-url")
  val feedbackUrl: String               = config.get[String]("external-url.feedback-survey")

  val appName: String = config.get[String]("appName")

  private val emailVerificationBaseUrl: String = servicesConfig.baseUrl("email-verification")
  private val emailVerificationContext: String = config.get[String]("microservice.services.email-verification.context")
  private val emailVerificationWithContext     = s"$emailVerificationBaseUrl/$emailVerificationContext"
  val emailVerificationTemplateId: String      = config.get[String]("microservice.services.email-verification.templateId")

  val emailVerificationLinkExpiryDuration: String =
    config.get[String]("microservice.services.email-verification.linkExpiryDuration")

  private val customsDataStoreBaseUrl: String = servicesConfig.baseUrl("customs-data-store")
  private val customsDataStoreContext: String = config.get[String]("microservice.services.customs-data-store.context")
  val customsDataStoreUrl                     = s"$customsDataStoreBaseUrl$customsDataStoreContext"

  private val customsHodsProxyBaseUrl: String = servicesConfig.baseUrl("customs-email-proxy")

  private val subscriptionDisplayContext: String =
    config.get[String]("microservice.services.customs-email-proxy.subscription-display.context")

  val subscriptionDisplayUrl: String =
    s"$customsHodsProxyBaseUrl/$subscriptionDisplayContext"

  private val updateVerifiedEmailContext: String =
    config.get[String]("microservice.services.customs-email-proxy.update-verified-email.context")

  val updateVerifiedEmailUrl: String = s"$customsHodsProxyBaseUrl/$updateVerifiedEmailContext"

  lazy val checkVerifiedEmailUrl: String             = s"$emailVerificationWithContext/verified-email-check"
  lazy val createEmailVerificationRequestUrl: String = s"$emailVerificationWithContext/verification-requests"

  private val save4LaterContext: String =
    config.get[String]("microservice.services.customs-email-proxy.mongo-cache.context")

  lazy val save4LaterUrl: String = s"$customsHodsProxyBaseUrl/$save4LaterContext"

  lazy val referrerName: Seq[ReferrerName] = config.get[Seq[ReferrerName]]("referrer-services")

  lazy val customsFinanceReferrer: Option[ReferrerName] = referrerName.find(_.name == "customs-finance")

  lazy val traderGoodsProfilesReferrer: Option[ReferrerName] = referrerName.find(_.name == "trader-goods-profiles")

  lazy val timeout: Int             = config.get[Int]("timeout.timeout")
  lazy val countdown: Int           = config.get[Int]("timeout.countdown")
  lazy val loginContinueUrl: String = config.get[String]("external-url.loginContinue")
}

object AppConfig {
  implicit val configLoader: ConfigLoader[Seq[ReferrerName]] =
    ConfigLoader(_.getConfigList).map(
      _.asScala.toList
        .map(config => ReferrerName(config.getString("name"), config.getString("continueUrl")))
    )
}
