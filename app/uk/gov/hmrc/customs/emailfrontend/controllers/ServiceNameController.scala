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

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.customs.emailfrontend.config.AppConfig
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.WhatIsYourEmailController
import uk.gov.hmrc.customs.emailfrontend.model.ReferrerName
import uk.gov.hmrc.customs.emailfrontend.services.EmailCacheService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class ServiceNameController @Inject()(actions: Actions,
                                      appConfig: AppConfig,
                                      emailCacheService: EmailCacheService,
                                      mcc: MessagesControllerComponents)
                                     (implicit override val messagesApi: MessagesApi, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def show(name: String): Action[AnyContent] = (actions.auth andThen actions.isPermitted andThen actions.isEnrolled).async { implicit request =>
    val optionalReferrerName: Option[ReferrerName] = appConfig.referrerName.find(_.name == name)
    optionalReferrerName.map { referrerName =>
      emailCacheService.saveReferrer(request.user.internalId, referrerName)
      Future.successful(Redirect(WhatIsYourEmailController.show()))
    }.getOrElse(Future.successful(Redirect(WhatIsYourEmailController.problemWithService())))
  }
}

