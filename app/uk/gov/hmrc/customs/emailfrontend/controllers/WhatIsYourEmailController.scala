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

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.customs.emailfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{CheckYourEmailController, WhatIsYourEmailController}
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html.{what_is_your_email, change_your_email}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatIsYourEmailController @Inject()(actions: Actions, view: change_your_email,
                                          verifyView: what_is_your_email,
                                          emailCacheService: EmailCacheService,
                                          mcc: MessagesControllerComponents,
                                          subscriptionDisplayConnector: SubscriptionDisplayConnector,
                                          emailVerificationService: EmailVerificationService)
                                         (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = actions.auth.async { implicit request =>
    emailCacheService.fetchEmail(Some(request.user.internalId.id)) flatMap {
      _.fold {
        Logger.warn("[WhatIsYourEmailController][show] - emailStatus not found")
        Future.successful(Redirect(WhatIsYourEmailController.create()))
      } {
        _ =>
          Future.successful(Redirect(CheckYourEmailController.show()))
      }
    }
  }

  def create: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit request =>
    subscriptionDisplayConnector.subscriptionDisplay(Eori(request.eori.id)).flatMap {
      case SubscriptionDisplayResponse(Some(email)) => {
        emailVerificationService.isEmailVerified(email).map {
          case Some(true) => Ok(view(emailForm, email))
          case Some(false) => Redirect(WhatIsYourEmailController.verify())
        }
      }
    }
  }

  def verify: Action[AnyContent] = actions.auth { implicit request =>
    Ok(verifyView(emailForm))
  }

  def submit: Action[AnyContent] = (actions.auth andThen actions.eori).async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => {
        subscriptionDisplayConnector.subscriptionDisplay(Eori(request.eori.id)).map {
          case SubscriptionDisplayResponse(Some(email)) => BadRequest(view(formWithErrors, email))
        }
      },
      formData => {
        emailCacheService.saveEmail(Some(request.user.internalId.id), EmailStatus(formData.value)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }
}
