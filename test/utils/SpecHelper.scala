package utils

import common.pages.BasePage
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.{Assertion, Matchers}
import org.scalatestplus.selenium.Page

import scala.util.Random

trait SpecHelper extends Matchers {

  lazy val webDriver: WebDriver = Configuration.webDriver
  private val continueButtonId: By = By.id("continue")

  def navigateTo: Page => Unit = page => webDriver.navigate().to(page.url)

  def waitForPresenceOfElement(locator:By): WebElement = {
    new WebDriverWait(webDriver,10).until(ExpectedConditions.presenceOfElementLocated(locator))
  }

  def verifyCurrentPage: BasePage => Assertion = page => {
    new WebDriverWait(webDriver, 5).until(ExpectedConditions.urlContains(page.url))
    assert(webDriver.getTitle contentEquals page.title, s"Page title: '${webDriver.getTitle}' not as expected")
  }

  def enterText(locator: By): String => Unit = text => {
    waitForPresenceOfElement(locator)
    webDriver.findElement(locator).sendKeys(text)
  }

  def assertIsTextVisible(locator: By) : String => Boolean = text => {
    new WebDriverWait(webDriver,10).until(ExpectedConditions.textToBePresentInElementLocated(locator,text))
  }

  def clickContinue(): Unit = {
    waitForPresenceOfElement(continueButtonId)
    webDriver.findElement(continueButtonId).click()
  }

  def clickOn(locator:By): Unit = {
    waitForPresenceOfElement(locator)
    webDriver.findElement(locator).click()
  }

  def generateRandomNumberString(): String = (1 to 10).map(_ => Random.nextInt(10)).mkString

}
