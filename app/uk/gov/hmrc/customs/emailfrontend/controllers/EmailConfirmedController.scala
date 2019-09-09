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

package uk.gov.hmrc.customs.emailfrontend.controllers

import javax.inject.Inject
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{SignOutController, VerifyYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.services.{CustomsDataStoreService, EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html.email_confirmed
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class EmailConfirmedController @Inject()(actions: Actions, view: email_confirmed,
                                         customsDataStoreService: CustomsDataStoreService,
                                         emailCacheService: EmailCacheService,
                                         emailVerificationService: EmailVerificationService,
                                         mcc: MessagesControllerComponents)
                                        (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = actions.auth.async { implicit request =>
    emailCacheService.fetchEmail(Some(request.user.internalId.id)) flatMap {
      _.fold {
        Logger.warn("[EmailConfirmedController][show] - emailStatus cache none, user logged out")
        Future.successful(Redirect(SignOutController.signOut()))
      } {
        emailStatus =>
          emailVerificationService.isEmailVerified(emailStatus.email).map {
            case Some(true) =>
              request.user.eori.map(identifier => customsDataStoreService.storeEmail(identifier, emailStatus.email))
              Ok(view())
            case _ => Redirect(VerifyYourEmailController.show())
          }
      }
    }
  }
}
