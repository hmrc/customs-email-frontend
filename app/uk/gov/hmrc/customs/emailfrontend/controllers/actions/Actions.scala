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

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, EoriRequest}

import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[ActionsImpl])
trait Actions {
  def auth: ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest]
  def isPermitted : ActionFilter[AuthenticatedRequest]
  def isEnrolled : ActionFilter[AuthenticatedRequest]
  def eori: ActionRefiner[AuthenticatedRequest, EoriRequest]
  def unauthorised: DefaultActionBuilder
}

@Singleton
class ActionsImpl @Inject()(authConnector: AuthConnector, config: Configuration, environment: Environment, mcc: MessagesControllerComponents)
                           (implicit ec: ExecutionContext) extends Actions {

  private def bodyParser = mcc.parsers.defaultBodyParser

  override def auth: ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest] = new AuthAction(authConnector, config, environment, bodyParser)

  override def isPermitted: ActionFilter[AuthenticatedRequest] = new PermittedUserFilter()

  override def isEnrolled: ActionFilter[AuthenticatedRequest] = new EnrolledUserFilter()

  override def eori: ActionRefiner[AuthenticatedRequest, EoriRequest] = new EoriAction()

  override def unauthorised: DefaultActionBuilder = new UnauthorisedAction(bodyParser)
}