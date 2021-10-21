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

///*
// * Copyright 2021 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.customs.emailfrontend.controllers
//
//import play.api.i18n.Lang
//import uk.gov.hmrc.play.language.LanguageUtils
//
//class EmailLanguageControllerSpec extends ControllerSpec {
//
//  private val languageUtil = app.injector.instanceOf[LanguageUtils]
//
//  val controller = new EmailLanguageController(languageUtil, cc)
//
//  "Email Language Controller" should {
//
//    "return the page in English language" in {
//      val language = "english"
//      val langCall = controller.langToCall(language)
//      langCall(language) shouldBe routes.EmailLanguageController.switchToLanguage(language)
//    }
//
//    "return the page in Welsh language" in {
//      val language = "welsh"
//      val langCall = controller.langToCall(language)
//      langCall(language) shouldBe routes.EmailLanguageController.switchToLanguage(language)
//    }
//
//    "have mapped english and welsh for language map" in {
//      controller.languageMap.get("english") shouldBe Some(Lang("en"))
//      controller.languageMap.get("cymraeg") shouldBe Some(Lang("cy"))
//    }
//  }
//}
