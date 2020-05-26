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
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.Actions
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.SignOutController
import uk.gov.hmrc.customs.emailfrontend.services.Save4LaterService
import uk.gov.hmrc.customs.emailfrontend.views.html.amendment_in_progress
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext

class AmendmentInProgressController @Inject()(
  actions: Actions,
  view: amendment_in_progress,
  save4LaterService: Save4LaterService,
  mcc: MessagesControllerComponents
)(implicit override val messagesApi: MessagesApi, ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] =
    (actions.auth andThen actions.isPermitted andThen actions.isEnrolled).async { implicit request =>
      save4LaterService.fetchEmail(request.user.internalId) map {
        _.fold {
          Logger.warn("[AmendmentInProgressController][show] - emailStatus not found")
          Redirect(SignOutController.signOut())
        } { emailDetails =>
          Ok(view(emailDetails.newEmail))
        }
      }
    }
}
