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

package uk.gov.hmrc.customs.emailfrontend.acceptance.specs

import org.openqa.selenium.By
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.scalatest.{Assertion, Matchers}
import org.scalatestplus.selenium.Page
import uk.gov.hmrc.customs.emailfrontend.acceptance.pages.BasePage
import uk.gov.hmrc.customs.emailfrontend.acceptance.pages.utils.Configuration

trait SpecHelper extends Matchers {

  lazy val webDriver = Configuration.webDriver

  def navigateTo: Page => Unit = page => webDriver.navigate().to(page.url)

  def verifyCurrentPage: BasePage => Assertion = page => {
    new WebDriverWait(webDriver, 5).until(ExpectedConditions.urlContains(page.url))
    assert(webDriver.getTitle contentEquals page.title, s"Page title: '${webDriver.getTitle}' not as expected")
  }

  def enterText(locator: By): String => Unit = text => webDriver.findElement(locator).sendKeys(text)

  def assertIsTextVisible(locator: By) : String => Boolean = text => new WebDriverWait(webDriver,10).until(ExpectedConditions.textToBePresentInElementLocated(locator,text))

  def clickContinue() = webDriver.findElement(By.id("continue")).click()

}
