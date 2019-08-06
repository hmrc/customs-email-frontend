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

package uk.gov.hmrc.customs.emailfrontend.unit

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.reset

import scala.concurrent.{ExecutionContext, Future}

trait AuthBuilder {

  this: MockitoSugar =>

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  private val notLoggedInException = new NoActiveSession("A user is not logged in") {}

  private val internId = Some("internalId")

  def resetAuthConnector(): Unit = reset(mockAuthConnector)

  def withAuthorisedUser(eori: Eori, userInternalId: Option[String] = internId)(test: => Unit) {
    val userEnrollments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori.id)))

    when(mockAuthConnector.authorise(any(), meq(allEnrolments and internalId))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(new ~(userEnrollments, userInternalId)))
    test
  }

  def withAuthorisedUserWithoutEnrolments(test: => Unit) {
    when(mockAuthConnector.authorise(any(), meq(allEnrolments and internalId))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(new ~(Enrolments(Set.empty[Enrolment]), internId)))
    test
  }

  def withAuthorisedUserWithoutEori(test: => Unit) {
    when(mockAuthConnector.authorise(any(), meq(allEnrolments and internalId))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.failed(InsufficientEnrolments("Some Message")))
    test
  }

  def withUnauthorisedUserWithoutInternalId(test: => Unit) {
    withAuthorisedUser(Eori("ZZ111111111"), None)(test)
  }

  def withUnauthorisedUser(test: => Unit) {
    when(mockAuthConnector.authorise(any[AuthProviders], any())(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.failed(notLoggedInException))
    test
  }
}
