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

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, when}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.auth.core.AffinityGroup.{Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, allEnrolments, credentialRole, internalId}
import uk.gov.hmrc.auth.core.retrieve.{~ => Retrieve}
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait AuthBuilder {

  this: MockitoSugar =>

  import uk.gov.hmrc.customs.emailfrontend.Retrieval._

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  private val notLoggedInException = new NoActiveSession("A user is not logged in") {}

  private val internId = Some("internalId")

  def resetAuthConnector(): Unit = reset(mockAuthConnector)

  def withAuthorisedUser(eori: Eori = Eori("GB1234567890"), userInternalId: Option[String] = internId)(test: => Unit) {
    val userEnrollments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori.id)))
    val ag = Some(Organisation)
    val role = Some(Admin)
    val retrieval = Retrieve(userEnrollments , userInternalId).add(ag).add(role)
    when(mockAuthConnector.authorise(any(), meq(allEnrolments and internalId and affinityGroup and credentialRole))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(retrieval))
    test
  }

  def withAuthorisedIndividualUser(eori: Eori = Eori("GB1234567890"), userInternalId: Option[String] = internId)(test: => Unit) {
    val userEnrollments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori.id)))
    val ag = Some(Individual)
    val role = Some(User)
    val retrieval =  Retrieve(userEnrollments , userInternalId).add(ag).add(role)
    when(mockAuthConnector.authorise(any(), meq(allEnrolments and internalId and affinityGroup and credentialRole))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(retrieval))
    test
  }


  def withAuthorisedUserWithoutEnrolments(test: => Unit) {
    val ag = Some(Organisation)
    val role = Some(Admin)
    val retrieval = Retrieve(Enrolments(Set.empty[Enrolment]), internId).add(ag).add(role)
    when(mockAuthConnector.authorise(any(), meq(allEnrolments and internalId and affinityGroup and credentialRole))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(retrieval))
    test
  }



  def withAuthorisedUserWithoutEori(test: => Unit) {
    when(mockAuthConnector.authorise(any(), meq(allEnrolments and internalId and affinityGroup and credentialRole))(any[HeaderCarrier], any[ExecutionContext]))
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


