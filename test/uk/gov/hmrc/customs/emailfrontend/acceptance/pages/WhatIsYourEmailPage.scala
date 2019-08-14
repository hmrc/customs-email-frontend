package uk.gov.hmrc.customs.emailfrontend.acceptance.pages

import uk.gov.hmrc.customs.emailfrontend.acceptance.pages.utils.Configuration

class WhatIsYourEmailPage extends BasePage {
  def navigate() = Configuration.webDriver.navigate().to("http://localhost:9000/customs-email-frontend/start")
}

object WhatIsYourEmailPage extends WhatIsYourEmailPage
