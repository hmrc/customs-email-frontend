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

package uk.gov.hmrc.customs.emailfrontend.controllers.actions

import play.api.Logger
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, Ineligible}

import scala.concurrent.{ExecutionContext, Future}

class EnrolledUserFilter(implicit override val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {
    if(request.user.eori.nonEmpty) Future.successful(None)
    else {
      Logger.warn("[EnrolledUserFilter] CDS Enrolment is missing")
      Future.successful(Some(Redirect(IneligibleUserController.show(Ineligible.NoEnrolment))))
    }
  }
}
