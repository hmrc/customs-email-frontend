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

package uk.gov.hmrc.customs.emailfrontend.controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class SignOutController @Inject() (appConfig: AppConfig, mcc: MessagesControllerComponents)(implicit
  override val messagesApi: MessagesApi
) extends FrontendController(mcc)
    with I18nSupport {

  def signOut: Action[AnyContent] = Action {
    Redirect(appConfig.feedbackUrl).withNewSession
  }

  def logoutNoSurvey: Action[AnyContent] = Action {
    Redirect(appConfig.loginContinueUrl).withNewSession
  }
}
