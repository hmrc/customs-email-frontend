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
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, LoggedInUser}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

import scala.concurrent.{ExecutionContext, Future}

class AuthAction(predicate: Either[AuthProvider, Enrolment], auth: AuthConnector, override val config: Configuration, override val env: Environment, defaultBodyParser: BodyParser[AnyContent])(implicit override val executionContext: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] with ActionRefiner[Request, AuthenticatedRequest] with AuthorisedFunctions with AuthRedirects {

  override def authConnector: AuthConnector = auth

  override def parser: BodyParser[AnyContent] = defaultBodyParser

  override protected def refine[A](request: Request[A]): Future[Either[Result, AuthenticatedRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, Some(request.session))
    authorised(predicate.fold(authProvider => AuthProviders(authProvider), enrolment => enrolment))
      .retrieve(allEnrolments)(
        userAllEnrolments =>
          Future.successful(Right(AuthenticatedRequest(request, LoggedInUser(userAllEnrolments))))
      ) recover (withRedirect(request) orElse withAuth(request))
  }

  private def withRedirect(implicit request: Request[_]): PartialFunction[Throwable, Either[Result, Nothing]] = {
    case _: InsufficientEnrolments => Left(Redirect(IneligibleUserController.show()))
  }

  private def withAuth(implicit request: Request[_]): PartialFunction[Throwable, Either[Result, Nothing]] = {
    case _: NoActiveSession => Left(toGGLogin(continueUrl = "/"))
  }
}