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

package uk.gov.hmrc.customs.emailfrontend.utils

import play.api.mvc.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.{AffinityGroup, User}
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.IdentifierAction
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, InternalId, LoggedInUser}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait FakeIdentifierAction extends IdentifierAction {

  protected val affinityGroup: AffinityGroup
  protected val parsers: PlayBodyParsers

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
    block(
      AuthenticatedRequest(
        request,
        LoggedInUser(InternalId("fakeInternalId"), Some(affinityGroup), Some(User), "fakeEori")
      )
    )

  override def parser: BodyParser[AnyContent] = parsers.default

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

class FakeIdentifierAgentAction @Inject() (bodyParsers: PlayBodyParsers) extends FakeIdentifierAction {
  override protected val affinityGroup: AffinityGroup = Agent
  override protected val parsers: PlayBodyParsers     = bodyParsers
}

class FakeIdentifierIndividualAction @Inject() (bodyParsers: PlayBodyParsers) extends FakeIdentifierAction {
  override protected val affinityGroup: AffinityGroup = Individual
  override protected val parsers: PlayBodyParsers     = bodyParsers
}

class FakeIdentifierOrganisationAction @Inject() (bodyParsers: PlayBodyParsers) extends FakeIdentifierAction {
  override protected val affinityGroup: AffinityGroup = Organisation
  override protected val parsers: PlayBodyParsers     = bodyParsers
}
