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

import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, EmailDetails}
import uk.gov.hmrc.customs.emailfrontend.services.*
import uk.gov.hmrc.customs.emailfrontend.viewmodels.EmailVerifiedOrChangedViewModel
import uk.gov.hmrc.customs.emailfrontend.views.html.email_verified_or_changed
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EmailConfirmedController @Inject()(identify: IdentifierAction,
                                         emailVerifiedOrChangedView: email_verified_or_changed,
                                         customsDataStoreService: CustomsDataStoreService,
                                         save4LaterService: Save4LaterService,
                                         emailVerificationService: EmailVerificationService,
                                         updateVerifiedEmailService: UpdateVerifiedEmailService,
                                         mcc: MessagesControllerComponents,
                                         errorHandler: ErrorHandler,
                                         dateTimeService: DateTimeService,
                                         appConfig: AppConfig)
                                        (implicit override val messagesApi: MessagesApi,
                                         ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with Logging {

  def show: Action[AnyContent] =
    identify.async { implicit request =>
      save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        redirectBasedOnEmailStatus,
        Future.successful(Redirect(routes.SignOutController.signOut))
      )
    }

  private def redirectBasedOnEmailStatus(details: EmailDetails)
                                        (implicit request: AuthenticatedRequest[_]): Future[Result] =
    for {
      verified <- emailVerificationService.isEmailVerified(details.newEmail)
      redirect <- if (verified.contains(true)) {
        updateEmail(details)
      } else {
        Future.successful(Redirect(routes.VerifyYourEmailController.show))
      }
    } yield redirect

  private def updateEmail(details: EmailDetails)(implicit request: AuthenticatedRequest[_]): Future[Result] = {
    val timestamp = dateTimeService.nowUtc()

    lazy val viewModel = for {
      _ <- save4LaterService.saveEmail(request.user.internalId, details.copy(timestamp = Some(timestamp)))

      _ <- customsDataStoreService.storeEmail(
        EnrolmentIdentifier("EORINumber", request.user.eori), details.newEmail, timestamp)

      maybeReferrerName <- save4LaterService.fetchReferrer(request.user.internalId)
      journeyType <- save4LaterService.fetchJourneyType(request.user.internalId)
    } yield EmailVerifiedOrChangedViewModel(
      details.newEmail,
      maybeReferrerName.map(_.continueUrl),
      journeyType.fold {
        logger.warn("Unable to determine journey type!")
        true } { _.isVerify },
      appConfig
    )

    updateVerifiedEmailService
      .updateVerifiedEmail(details.currentEmail, details.newEmail, request.user.eori, timestamp)
      .flatMap {
        case Some(true) => viewModel.map(vm => Ok(emailVerifiedOrChangedView(vm)))
          .recover { case _ => Redirect(routes.EmailConfirmedController.problemWithService()) }

        case _ =>
          Future.successful(Redirect(routes.EmailConfirmedController.problemWithService()))
      }
  }

  def problemWithService(): Action[AnyContent] = identify.async { implicit request =>
    Future.successful(BadRequest(errorHandler.problemWithService()))
  }
}
