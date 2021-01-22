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

package uk.gov.hmrc.customs.emailfrontend.controllers

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.SignOutController
import uk.gov.hmrc.customs.emailfrontend.model.{EmailDetails, EoriRequest}
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterService
import uk.gov.hmrc.customs.emailfrontend.views.html.verify_your_email
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class VerifyYourEmailController @Inject()(
                                           actions: Actions,
                                           view: verify_your_email,
                                           save4LaterService: Save4LaterService,
                                           mcc: MessagesControllerComponents
)(implicit override val messagesApi: MessagesApi, ex: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] =
    (actions.auth
      andThen actions.isPermitted
      andThen actions.isEnrolled).async { implicit request =>
      save4LaterService.routeBasedOnAmendment(request.user.internalId)(
        redirectWithEmail,
        Future.successful(Redirect(SignOutController.signOut()))
      )
    }

  private def redirectWithEmail(details: EmailDetails)(implicit request: EoriRequest[AnyContent]): Future[Result] =
    Future.successful(Ok(view(details.newEmail)))
}
