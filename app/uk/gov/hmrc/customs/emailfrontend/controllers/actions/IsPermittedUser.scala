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

import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.{Admin, AffinityGroup, CredentialRole, User}
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, Ineligible}

import scala.concurrent.{ExecutionContext, Future}

class IsPermittedUser(implicit override val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {

    val affinityGroup: Option[AffinityGroup] = request.user.affinityGroup
    val credentialRole: Option[CredentialRole] = request.user.credentialRole

    (affinityGroup,credentialRole) match {
      case(Some(Organisation),Some(Admin) | Some(User)) => toFutureResult()
      case(Some(Organisation), _) => toFutureResult(Some(Ineligible.NotAdmin))
      case(Some(Agent),_) => toFutureResult(Some(Ineligible.IsAgent))
      case _ => toFutureResult()
    }

  }

  private def toFutureResult(result:Option[Ineligible.Value] = None): Future[Option[Result]] = {
    Future.successful(result.map(i => Redirect(IneligibleUserController.show(i))))
  }


}
