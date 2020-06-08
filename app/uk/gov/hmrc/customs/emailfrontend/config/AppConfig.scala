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

package uk.gov.hmrc.customs.emailfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.customs.emailfrontend.model.ReferrerName
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.collection.JavaConverters._

@Singleton
class AppConfig @Inject()(config: Configuration, servicesConfig: ServicesConfig) {
  
  private val contactBaseUrl = servicesConfig.baseUrl("contact-frontend")

  private val assetsUrl = config.get[String]("assets.url")
  private val serviceIdentifier = config.get[String]("microservice.services.contact-frontend.serviceIdentifier")

  val assetsPrefix: String = assetsUrl + config.get[String]("assets.version")
  val analyticsToken: String = config.get[String](s"google-analytics.token")
  val analyticsHost: String = config.get[String](s"google-analytics.host")

  val reportAProblemPartialUrl: String = s"$contactBaseUrl/contact/problem_reports_ajax?service=$serviceIdentifier"
  val reportAProblemNonJSUrl: String = s"$contactBaseUrl/contact/problem_reports_nonjs?service=$serviceIdentifier"

  val ggSignInRedirectUrl: String =
    config.get[String]("external-url.company-auth-frontend.continue-url")
  val feedbackUrl: String = config.get[String]("external-url.feedback-survey")

  val appName: String = config.get[String]("appName")

  val emailVerificationBaseUrl: String =
    servicesConfig.baseUrl("email-verification")
  val emailVerificationContext: String =
    config.get[String]("microservice.services.email-verification.context")
  val emailVerificationWithContext =
    s"${emailVerificationBaseUrl}/${emailVerificationContext}"
  val emailVerificationTemplateId: String =
    config.get[String]("microservice.services.email-verification.templateId")
  val emailVerificationLinkExpiryDuration: String =
    config.get[String]("microservice.services.email-verification.linkExpiryDuration")

  val customsDataStoreBaseUrl: String =
    servicesConfig.baseUrl("customs-data-store")
  val customsDataStoreContext: String =
    config.get[String]("microservice.services.customs-data-store.context")
  val customsDataStoreUrl = s"$customsDataStoreBaseUrl$customsDataStoreContext"
  val customsDataStoreToken: String =
    config.get[String]("microservice.services.customs-data-store.token")

  val customsHodsProxyBaseUrl: String =
    servicesConfig.baseUrl("customs-email-proxy")

  val subscriptionDisplayContext: String =
    config.get[String]("microservice.services.customs-email-proxy.subscription-display.context")
  val subscriptionDisplayUrl: String =
    s"$customsHodsProxyBaseUrl/$subscriptionDisplayContext"

  val updateVerifiedEmailContext: String =
    config.get[String]("microservice.services.customs-email-proxy.update-verified-email.context")
  val updateVerifiedEmailUrl: String =
    s"$customsHodsProxyBaseUrl/$updateVerifiedEmailContext"

  val save4LaterContext: String =
    config.get[String]("microservice.services.customs-email-proxy.mongo-cache.context")
  val save4LaterUrl: String = s"$customsHodsProxyBaseUrl/$save4LaterContext"

  implicit val configLoader: ConfigLoader[Seq[ReferrerName]] =
    ConfigLoader(_.getConfigList).map(
      _.asScala.toList
        .map(config => ReferrerName(config.getString("name"), config.getString("continueUrl")))
    )
  lazy val referrerName: Seq[ReferrerName] =
    config.get[Seq[ReferrerName]]("referrer-services")
}
