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

import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Unauthorized
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, Eori, EoriRequest}

import scala.concurrent.{ExecutionContext, Future}

class EoriAction(implicit override val executionContext: ExecutionContext) extends ActionRefiner[AuthenticatedRequest, EoriRequest] {
  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, EoriRequest[A]]] = {
    Future.successful(request.user.eori map (eori => EoriRequest(request, Eori(eori))) toRight Unauthorized("No Eori"))
  }
}