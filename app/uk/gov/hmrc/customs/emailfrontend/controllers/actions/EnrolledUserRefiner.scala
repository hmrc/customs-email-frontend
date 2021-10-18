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

package uk.gov.hmrc.customs.emailfrontend.controllers.actions

import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.auth.core.EnrolmentIdentifier
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, Eori, EoriRequest, Ineligible}

import scala.concurrent.{ExecutionContext, Future}

class EnrolledUserRefiner(implicit override val executionContext: ExecutionContext)
    extends ActionRefiner[AuthenticatedRequest, EoriRequest] with Logging {
  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, EoriRequest[A]]] = {

    def enrolmentExists: Option[EnrolmentIdentifier] => Option[EoriRequest[A]] = {
      case Some(eori) if eori.value.nonEmpty => Some(EoriRequest(request, Eori(eori)))
      case _                                 => None
    }

    Future.successful(enrolmentExists(request.user.eori) orElse None toRight {
      logger.warn("CDS Enrolment is missing")
      Redirect(IneligibleUserController.show(Ineligible.NoEnrolment))
    })
  }
}
