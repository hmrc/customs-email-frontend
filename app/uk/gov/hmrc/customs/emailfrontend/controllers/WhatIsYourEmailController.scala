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
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.{CheckYourEmailController, AmendmentInProgressController,  WhatIsYourEmailController,EmailConfirmedController}
import uk.gov.hmrc.customs.emailfrontend.forms.Forms.emailForm
import uk.gov.hmrc.customs.emailfrontend.model._
import uk.gov.hmrc.customs.emailfrontend.services.{EmailCacheService, EmailVerificationService}
import uk.gov.hmrc.customs.emailfrontend.views.html.{change_your_email, what_is_your_email}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatIsYourEmailController @Inject()(actions: Actions, view: change_your_email,
                                          whatIsYourEmailView: what_is_your_email,
                                          emailCacheService: EmailCacheService,
                                          mcc: MessagesControllerComponents,
                                          subscriptionDisplayConnector: SubscriptionDisplayConnector,
                                          emailVerificationService: EmailVerificationService)
                                         (implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = (actions.authEnrolled andThen actions.isPermitted).async { implicit request =>
    for {
     status <- emailCacheService.emailAmendmentStatus(request.user.internalId)
     result <- redirectBasedOnAmendmentStatus(status)
    }yield {
      result
    }
  }

  private def redirectBasedOnAmendmentStatus(amendmentStatus: EmailAmendmentStatus)
                                            (implicit request: AuthenticatedRequest[AnyContent],
                                             hc: HeaderCarrier,
                                             executionContext: ExecutionContext):Future[Result]= {
    amendmentStatus match {
      case AmendmentInProgress => {
        Logger.warn("[WhatIsYourEmailController][show] - AmendmentInProgress")
        Future.successful(Redirect(AmendmentInProgressController.show()))
      }
      case AmendmentNotDetermined | AmendmentCompleted => {
        Logger.warn("[WhatIsYourEmailController][show] - AmendmentNotDetermined")
        redirectBasedOnEmailStatus
      }
    }
  }


  private def redirectBasedOnEmailStatus(implicit request: AuthenticatedRequest[AnyContent],
                                         hc: HeaderCarrier,
                                         executionContext: ExecutionContext): Future[Result] = {
    emailCacheService.fetchEmail(request.user.internalId) flatMap {
      _.fold {
        Logger.warn("[WhatIsYourEmailController][show] - email not found")
        Future.successful(Redirect(WhatIsYourEmailController.create()))
      } {
        emailStatus =>
          emailVerificationService.isEmailVerified(emailStatus.email).map {
            case Some(true) => Redirect(EmailConfirmedController.show())
            case Some(false) => Redirect(WhatIsYourEmailController.verify())
            case None => ??? //ToDo redirect to retry page  Email Service is down or any other errors
          }
      }
    }
  }


  def create: Action[AnyContent] = (actions.authEnrolled andThen actions.eori).async { implicit request =>
    subscriptionDisplayConnector.subscriptionDisplay(Eori(request.eori.id)).flatMap {
      case SubscriptionDisplayResponse(Some(email)) =>
        emailVerificationService.isEmailVerified(email).map {
          case Some(true) => Ok(view(emailForm, email))
          case Some(false) => Redirect(WhatIsYourEmailController.verify())
          case None => ??? //ToDo redirect to retry page
        }
    }
  }

  def verify: Action[AnyContent] = actions.authEnrolled { implicit request =>
    Ok(whatIsYourEmailView(emailForm))
  }

  def submit: Action[AnyContent] = (actions.authEnrolled andThen actions.eori).async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => {
        subscriptionDisplayConnector.subscriptionDisplay(Eori(request.eori.id)).map {
          case SubscriptionDisplayResponse(Some(email)) => BadRequest(view(formWithErrors, email))
        }
      },
      formData => {
        emailCacheService.saveEmail(request.user.internalId, EmailStatus(formData.value)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }

  def verifySubmit: Action[AnyContent] = (actions.authEnrolled andThen actions.eori).async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(whatIsYourEmailView(formWithErrors))),
      formData => {
        emailCacheService.saveEmail(request.user.internalId, EmailStatus(formData.value)).map {
          _ => Redirect(routes.CheckYourEmailController.show())
        }
      }
    )
  }

  private[this] def redirectAccordingToTimestamp(internalId: InternalId)(implicit hc: HeaderCarrier): Future[Result] = {
    emailCacheService.emailAmendmentStatus(internalId).map {
      case AmendmentInProgress => Redirect(AmendmentInProgressController.show())
      case _ => Redirect(CheckYourEmailController.show())
    }
  }
}
