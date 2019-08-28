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

import acceptance.pages.BasePage
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.{Assertion, Matchers}
import org.scalatestplus.selenium.Page

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

  def assertIsTextVisible(locator: By) : String => Boolean = text => new WebDriverWait(webDriver,10).until(ExpectedConditions.textToBePresentInElementLocated(locator,text))

  def clickContinue(): Unit = {
    waitForPresenceOfElement(continueButtonId)
    webDriver.findElement(continueButtonId).click()
  }
  
}
