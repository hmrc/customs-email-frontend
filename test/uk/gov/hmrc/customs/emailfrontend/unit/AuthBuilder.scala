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
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.{AuthConnector, Enrolment, Enrolments}
import uk.gov.hmrc.customs.emailfrontend.model.Eori
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait AuthBuilder {

  this: MockitoSugar =>

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  def withAuthorisedUser(eori: Eori)(test: => Unit) {
    val userEnrollments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", eori.id)))

    when(mockAuthConnector.authorise(any(), meq(allEnrolments))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(userEnrollments))
    test
  }

  def withAuthorisedUserWithoutEnrolments(test: => Unit) {
    when(mockAuthConnector.authorise(any(), meq(allEnrolments))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(Enrolments(Set.empty)))
    test
  }

  def withAuthorisedUserWithoutEori(test: => Unit) {
    val userEnrollments: Enrolments = Enrolments(Set(Enrolment("HMRC-CUS-ORG")))

    when(mockAuthConnector.authorise(any(), meq(allEnrolments))(any[HeaderCarrier], any[ExecutionContext]))
      .thenReturn(Future.successful(userEnrollments))
    test
  }

}
