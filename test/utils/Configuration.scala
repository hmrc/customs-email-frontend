package utils

import TestEnvironment.{DEV, LOCAL, QA, TestEnvironment}
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
