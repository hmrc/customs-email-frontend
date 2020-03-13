/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.{Lang, MessagesApi}
import play.api.mvc.{Call, ControllerComponents}
import uk.gov.hmrc.play.language.{LanguageController, LanguageUtils}

@Singleton
class EmailLanguageController @Inject()(config: Configuration,
                                       languageUtils: LanguageUtils,
                                       cc: ControllerComponents) extends LanguageController(config, languageUtils, cc) {
  override protected def fallbackURL: String = "/customs/manage-email-cds"
  def langToCall(lang: String): String => Call = EmailLanguageController.routeToSwitchLanguage

  override def languageMap: Map[String, Lang] = EmailLanguageController.languageMap
}

object EmailLanguageController {
  def routeToSwitchLanguage: String => Call = (lang: String) => routes.EmailLanguageController.switchToLanguage(lang)

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )
}