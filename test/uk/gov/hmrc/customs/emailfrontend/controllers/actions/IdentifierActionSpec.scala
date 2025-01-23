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

package uk.gov.hmrc.customs.emailfrontend.controllers.actions

import com.google.inject.Inject
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Environment
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.customs.emailfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.customs.emailfrontend.controllers.routes
import uk.gov.hmrc.customs.emailfrontend.model.Ineligible
import uk.gov.hmrc.customs.emailfrontend.utils.SpecBase
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionSpec extends SpecBase {

  "Identifier Action" when {

    "redirect the user to ineligible (no-enrolment) when has no enrolments" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.successful(Enrolments(Set.empty) ~ Some("internalId") ~ Some(Organisation) ~ Some(User)))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)

      private val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(FakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.IneligibleUserController.show(Ineligible.NoEnrolment).url
      }
    }

    "redirect the user to ineligible when has no enrolments, internalId and Affinity are provided" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.successful(Enrolments(Set.empty) ~ None ~ None ~ None))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)

      private val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(FakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.IneligibleUserController.show(Ineligible.NoEnrolment).url
      }
    }

    "redirect the user to ineligible (no-enrolment) when has no eori enrolment" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]
      private val enrolments        = Set(Enrolment("someKey", Seq(EnrolmentIdentifier("someKey", "someValue")), "ACTIVE"))

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.successful(Enrolments(enrolments) ~ Some("internalId") ~ Some(Organisation) ~ Some(User)))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)

      private val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(FakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.IneligibleUserController.show(Ineligible.NoEnrolment).url
      }
    }

    "redirect the user (Organisation affinity group) to ineligible (not-admin) when has no credential role" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]

      private val enrolments = Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", "test")), "Active"))

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.successful(Enrolments(enrolments) ~ Some("internalId") ~ Some(Organisation) ~ None))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)

      private val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(FakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.IneligibleUserController.show(Ineligible.NotAdmin).url
      }
    }

    "redirect the user (Agent affinity group) to ineligible (is-agent) when has no credential role" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]
      private val enrolments        = Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", "test")), "Active"))

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.successful(Enrolments(enrolments) ~ Some("internalId") ~ Some(Agent) ~ None))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)
      private val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(FakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.IneligibleUserController.show(Ineligible.IsAgent).url
      }
    }

    "redirect the user to ineligible (no-enrolment) when has no affinity group" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]

      private val enrolments = Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", "test")), "Active"))

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.successful(Enrolments(enrolments) ~ Some("internalId") ~ None ~ Some(User)))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)
      private val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(FakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result)               shouldBe SEE_OTHER
        redirectLocation(result).get shouldBe routes.IneligibleUserController.show(Ineligible.NoEnrolment).url
      }
    }

    "redirect the user to unauthorised controller when an auth error happens" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.failed(new RuntimeException("something went wrong")))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)
      private val controller = new Harness(authAction)

      running(app) {
        val request = FakeRequest().withHeaders("X-Session-Id" -> "someSessionId")
        val result  = controller.onPageLoad()(request)
        status(result)          shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe errorHandler.problemWithService()(request).toString()
      }
    }

    "continue journey on successful response from auth" in new Setup {
      private val mockAuthConnector = mock[AuthConnector]

      private val enrolments = Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", "test")), "Active"))

      when(
        mockAuthConnector
          .authorise[Enrolments ~ Option[String] ~ Option[AffinityGroup] ~ Option[CredentialRole]](any, any)(any, any)
      ).thenReturn(Future.successful(Enrolments(enrolments) ~ Some("internalId") ~ Some(Organisation) ~ Some(User)))

      private val authAction =
        new AuthenticatedIdentifierAction(mockAuthConnector, config, env, errorHandler, bodyParsers)
      private val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(FakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result) shouldBe OK
      }
    }

    "the user hasn't logged in" should {
      "redirect to gov gateway sign in" in new Setup {

        private val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new MissingBearerToken),
          config,
          env,
          errorHandler,
          bodyParsers
        )

        private val controller = new Harness(authAction)
        private val result     = controller.onPageLoad()(FakeRequest())

        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should startWith("/bas-gateway/sign-in?continue_url=")
      }
    }

    "the user's session has expired" should {
      "redirect the user to log in " in new Setup {

        private val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new MissingBearerToken),
          config,
          env,
          errorHandler,
          bodyParsers
        )

        private val controller = new Harness(authAction)
        private val result     = controller.onPageLoad()(FakeRequest())

        status(result)             shouldBe SEE_OTHER
        redirectLocation(result).get should startWith("/bas-gateway/sign-in?continue_url=")
      }
    }

    "the user doesn't have sufficient enrolments" should {
      "redirect the user to the unauthorised page" in new Setup {

        private val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new InsufficientEnrolments),
          config,
          env,
          errorHandler,
          bodyParsers
        )

        private val controller = new Harness(authAction)
        private val result     = controller.onPageLoad()(FakeRequest())

        status(result)                 shouldBe SEE_OTHER
        redirectLocation(result).value shouldBe routes.IneligibleUserController.show(Ineligible.NoEnrolment).url
      }
    }
  }

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  implicit class Ops[A](a: A) {
    def ~[B](b: B): A ~ B = new ~(a, b)
  }

  trait Setup {
    protected val config: AppConfig                = app.injector.instanceOf[AppConfig]
    protected val bodyParsers: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
    protected val env: Environment                 = app.injector.instanceOf[Environment]
    protected val errorHandler: ErrorHandler       = app.injector.instanceOf[ErrorHandler]
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = emptyString

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
