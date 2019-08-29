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

package acceptance.utils

import acceptance.utils.TestEnvironment._
import org.openqa.selenium.WebDriver
import uk.gov.hmrc.webdriver.SingletonDriver

object Configuration {
  Option(System.getProperty("browser")) match {
    case Some("remote-chrome") =>
    case _ => System.setProperty("browser", "chrome")
  }

  lazy val webDriver: WebDriver = SingletonDriver.getInstance()

  private val defaultTestEnvironment = LOCAL

  private lazy val currentEnvironment = Option(System.getProperty("environment")) map withNameEither getOrElse Right(defaultTestEnvironment) match {
    case Left(message) => throw new IllegalArgumentException(message)
    case Right(value) => value
  }

  def forCurrentEnv[T](func: TestEnvironment => T): T = func(currentEnvironment)

  lazy val port: Int = forCurrentEnv {
    case QA | DEV => 80
    case LOCAL => Option(System.getProperty("port")).fold(9000)(_.toInt)
  }

  lazy val frontendHost: String = forCurrentEnv {
    case QA => "https://www.qa.tax.service.gov.uk"
    case DEV => "https://www.development.tax.service.gov.uk"
    case LOCAL => Option(System.getProperty("host")).getOrElse("http://localhost:" + port)
  }


}


