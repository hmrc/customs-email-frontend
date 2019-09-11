package uk.gov.hmrc.customs.emailfrontend.controllers.actions

import play.api.Logger
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.{Admin, AffinityGroup, CredentialRole, InsufficientEnrolments, User}
import uk.gov.hmrc.customs.emailfrontend.controllers.routes.IneligibleUserController
import uk.gov.hmrc.customs.emailfrontend.model.AuthenticatedRequest

import scala.concurrent.{ExecutionContext, Future}

class IsPermittedUser(implicit override val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {

  def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] = {

    val affinityGroup: Option[AffinityGroup] = request.user.affinityGroup
    val credentialRole: Option[CredentialRole] = request.user.credentialRole
    val isPermitted = (affinityGroup,credentialRole) match {
      case(Some(Agent),_) => false
      case(Some(Organisation),Some(Admin) | Some(User)) => true
      case(Some(Organisation), _) => false
      case _ => true
    }
    if (isPermitted) {
      Future.successful(None)
    }else{
      Logger.warn("User is a Agent or not Admin in Organisation")
      Future.successful(Some(Redirect(IneligibleUserController.show())))
    }
  }

}
