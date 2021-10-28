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

import com.google.inject.{ImplementedBy, Inject}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.customs.emailfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.customs.emailfrontend.controllers.routes
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, Ineligible, InternalId, LoggedInUser}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthenticatedIdentifierAction])
trait IdentifierAction extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionFunction[Request, AuthenticatedRequest]

class AuthenticatedIdentifierAction @Inject()(override val authConnector: AuthConnector,
                                              appConfig: AppConfig,
                                              override val env: Environment,
                                              errorHandler: ErrorHandler,
                                              val parser: BodyParsers.Default)
                                             (override implicit val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with AuthRedirects {

  override val config: Configuration = appConfig.config

  private lazy val ggSignInRedirectUrl: String = config.get[String]("external-url.company-auth-frontend.continue-url")

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(Retrievals.allEnrolments and Retrievals.internalId and Retrievals.affinityGroup and Retrievals.credentialRole) {
      case allEnrolments ~ Some(internalId) ~ affinityGroup ~ credentialRole =>
        (affinityGroup, credentialRole) match {
          case (Some(Organisation), None) =>
            Future.successful(Redirect(routes.IneligibleUserController.show(Ineligible.NotAdmin)))
          case (Some(Agent), _) =>
            Future.successful(Redirect(routes.IneligibleUserController.show(Ineligible.IsAgent)))
          case (Some(_), Some(User)) =>
            allEnrolments.getEnrolment("HMRC-CUS-ORG").flatMap(_.getIdentifier("EORINumber")) match {
              case Some(eori) =>
                val loggedInUser = LoggedInUser(InternalId(internalId), affinityGroup, credentialRole, eori.value)
                block(AuthenticatedRequest(request, loggedInUser))
              case _ => Future.successful(Redirect(routes.IneligibleUserController.show(Ineligible.NoEnrolment)))
            }
          case _ =>
            Future.successful(Redirect(routes.IneligibleUserController.show(Ineligible.NoEnrolment)))
        }
    }
  }.recover {
    case _: NoActiveSession => toGGLogin(continueUrl = ggSignInRedirectUrl)
    case _: InsufficientEnrolments => Redirect(routes.IneligibleUserController.show(Ineligible.NoEnrolment))
    case _: Throwable => InternalServerError(errorHandler.problemWithService()(request))

  }
}
