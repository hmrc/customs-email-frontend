package uk.gov.hmrc.customs.emailfrontend.acceptance.pages.utils

import org.openqa.selenium.WebDriver
import uk.gov.hmrc.webdriver.SingletonDriver

object Configuration {
  Option(System.getProperty("browser")) match {
    case Some("remote-chrome") =>
    case _ => System.setProperty("browser", "chrome")
  }

  lazy val webDriver: WebDriver = SingletonDriver.getInstance()

}
