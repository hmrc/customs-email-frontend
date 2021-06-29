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

package acceptance.specs

import java.net.URLEncoder
import com.typesafe.config.Config

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.openqa.selenium.Cookie
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.Codecs
import uk.gov.hmrc.crypto.{ApplicationCrypto, CryptoGCMWithKeysFromConfig, PlainText}
import uk.gov.hmrc.http.SessionKeys
import utils.{Configuration, Constants, WireMockRunner}
import utils.Configuration.webDriver

trait AcceptanceTestSpec
  extends AnyFeatureSpec
    with  GivenWhenThen
    with GuiceOneServerPerSuite
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with WireMockRunner {

  //override lazy val port = Configuration.port //todo

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "microservice.services.auth.port" -> Constants.wireMockPort,
        "microservice.services.cachable.short-lived-cache.port" -> Constants.wireMockPort,
        "microservice.services.email-verification.port" -> Constants.wireMockPort,
        "microservice.services.customs-data-store.port" -> Constants.wireMockPort,
        "microservice.services.customs-email-proxy.port" -> Constants.wireMockPort
      )
    )
    .disable[com.kenshoo.play.metrics.PlayModule]
    .build()

  private def encryptSessionData(sessionData: Map[String, String]): String = {
    val config: Config = app.configuration.underlying
    lazy val sessionCookieCrypto: CryptoGCMWithKeysFromConfig =
      new ApplicationCrypto(config).SessionCookieCrypto
    val UTF_8 = "UTF-8"
    lazy val provider: Option[String] =
      app.configuration.getOptional[String]("application.crypto.provider")
    lazy val applicationSecret = app.configuration
      .getOptional[String]("play.crypto.secret")
      .getOrElse("some random secret")
      .getBytes
    def sign(message: String, key: Array[Byte]): String = {
      val mac = provider
        .map(p => Mac.getInstance("HmacSHA1", p))
        .getOrElse(Mac.getInstance("HmacSHA1"))
      mac.init(new SecretKeySpec(key, "HmacSHA1"))
      Codecs.toHexString(mac.doFinal(message.getBytes(UTF_8)))
    }

    def encode(data: Map[String, String]): String = {
      val encoded = data
        .map {
          case (k, v) =>
            URLEncoder.encode(k, UTF_8) + "=" + URLEncoder.encode(v, UTF_8)
        }
        .mkString("&")
      sign(encoded, applicationSecret) + "-" + encoded
    }
    sessionCookieCrypto.encrypt(PlainText(encode(sessionData))).value
  }

  def addUserInSession() =
    webDriver manage () addCookie new Cookie(
      "mdtp",
      encryptSessionData(Map(SessionKeys.authToken -> "Bearer randomtoken"))
    )

  override def beforeAll: Unit =
    startMockServer()

  override def beforeEach(): Unit =
    resetMockServer()

  override def afterAll: Unit =
    stopMockServer()

  sys.addShutdownHook(webDriver.quit())
}
