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

package uk.gov.hmrc.customs.emailfrontend

import org.scalatest.matchers.must.Matchers.mustBe
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.{emptyString, singleSpace}

class PackageSpec extends SpecBase {
  "Utils.replaceSpaceWithEmptyString" should {
    "replace the spaces with empty string" in new SetUp {
      Utils.stripWhiteSpaces(stringWithOnlySpaces) mustBe emptyString
      Utils.stripWhiteSpaces(stringWithLeadingSpaces) mustBe "abc13456"
      Utils.stripWhiteSpaces(stringWithTrailingSpaces) mustBe "abc13456"
      Utils.stripWhiteSpaces(stringWithSpacesWithIn_1) mustBe "abt@test.com"
      Utils.stripWhiteSpaces(stringWithSpacesWithIn_2) mustBe "abt@test.com"
      Utils.stripWhiteSpaces(stringWithSpacesWithIn_3) mustBe "abt@test.com"
      Utils.stripWhiteSpaces(stringWithSpacesWithIn_4) mustBe "abt@test.com"
    }
  }
}

trait SetUp {
  val stringWithOnlySpaces     = singleSpace * 6
  val stringWithLeadingSpaces  = "  abc13456"
  val stringWithTrailingSpaces = "abc13456   "
  val stringWithSpacesWithIn_1 = "abt@ test.com"
  val stringWithSpacesWithIn_2 = "a b t@ test.com"
  val stringWithSpacesWithIn_3 = "abt@test. com"
  val stringWithSpacesWithIn_4 = "ab   t@   test. com"
}
