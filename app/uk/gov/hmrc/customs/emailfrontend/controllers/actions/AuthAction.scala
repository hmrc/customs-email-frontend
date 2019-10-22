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
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, credentialRole, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, Ineligible, InternalId, LoggedInUser}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

class AuthAction(override val authConnector: AuthConnector,
                 override val config: Configuration,
                 override val env: Environment,
                 override val parser: BodyParser[AnyContent])(implicit val executionContext: ExecutionContext)
  extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest] with AuthorisedFunctions with AuthRedirects {

  private lazy val ggSignInRedirectUrl: String = config.get[String]("external-url.company-auth-frontend.continue-url")

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))

    authorised(AuthProviders(GovernmentGateway)).retrieve(allEnrolments and internalId and affinityGroup and credentialRole) {
      case userAllEnrolments ~ Some(userInternalId) ~ affinityGroup ~ credentialRole =>
        Future.successful(Right(AuthenticatedRequest(request, LoggedInUser(userAllEnrolments, InternalId(userInternalId), affinityGroup, credentialRole))))
      case _ => {
        Logger.warn("AuthAction[refine] internalId or allEnrolments is missing")
        throw InsufficientEnrolments()
      }
    } recover withAuthOrRedirect(request)
  }

  private def withAuthOrRedirect[A](implicit request: Request[_]): PartialFunction[Throwable, Either[Result, A]] = {
    case _: NoActiveSession => Left(toGGLogin(continueUrl = ggSignInRedirectUrl))
    case _: InsufficientEnrolments => Left(Redirect(IneligibleUserController.show(Ineligible.NoEnrolment)))
  }
}