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

import play.api.Logger
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.AmendmentInProgressController
import uk.gov.hmrc.customs.emailfrontend.model._

import scala.concurrent.Future

trait DetermineRouteController{

   def redirectBasedOnAmendmentStatus(amendmentStatus: EmailAmendmentStatus)
                                     (redirectBasedOnEmailStatus: => Future[Result]): Future[Result] =
    amendmentStatus match {
      case AmendmentInProgress =>
        Logger.warn("[WhatIsYourEmailController][show] - AmendmentInProgress")
        Future.successful(Redirect(AmendmentInProgressController.show()))
      case AmendmentNotDetermined | AmendmentCompleted =>
        Logger.warn("[WhatIsYourEmailController][show] - AmendmentNotDetermined")
        redirectBasedOnEmailStatus
    }
}
