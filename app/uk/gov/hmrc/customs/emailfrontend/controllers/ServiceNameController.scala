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

import cats.data.OptionT.{fromOption, liftF}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ServiceNameController @Inject()(identify: IdentifierAction,
                                      appConfig: AppConfig,
                                      save4LaterService: Save4LaterService,
                                      mcc: MessagesControllerComponents)
                                     (implicit override val messagesApi: MessagesApi,
                                      ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(name: String): Action[AnyContent] =
    identify.async { implicit request =>
      (for {
        referrerName <- fromOption[Future](appConfig.referrerName.find(_.name == name))
        _ <- liftF(save4LaterService.saveReferrer(request.user.internalId, referrerName))
      }
      yield {
        Redirect(routes.WhatIsYourEmailController.show)
      }).getOrElse(Redirect(routes.WhatIsYourEmailController.show))
    }
}

