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

package unit

import play.api.i18n.MessagesApi
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.{Actions, AuthAction, EoriAction, IsPermittedUser, UnauthorisedAction}
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, EoriRequest}

import scala.concurrent.ExecutionContext

class FakeAction(authConnector: AuthConnector, bodyParser: BodyParser[AnyContent])
                (implicit messages: MessagesApi, ec: ExecutionContext) extends Actions {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  val userEnrollments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", "ZZ123456789")))

  override def authEnrolled: ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest] = new AuthAction(Right(Enrolment("HMRC-CUS-ORG")), authConnector, configuration, env, bodyParser)

  override def eori: ActionRefiner[AuthenticatedRequest, EoriRequest] = new EoriAction()

  override def auth: ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest] = new AuthAction(Left(GovernmentGateway), authConnector, configuration, env, bodyParser)

  override def unauthorised: DefaultActionBuilder = new UnauthorisedAction(bodyParser)

  override def isPermitted: ActionFilter[AuthenticatedRequest] = new IsPermittedUser()
}
