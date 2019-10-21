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

package unit.actions

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import play.api.http.HttpEntity
import play.api.mvc.{ResponseHeader, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.customs.emailfrontend.controllers.actions.EnrolledUserFilter
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, InternalId, LoggedInUser}

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolledUserFilterSpec extends PlaySpec with ScalaFutures {

  def responseHeader(e:String) = ResponseHeader(303, Map("Location" -> "/manage-email-cds/ineligible/no-enrolment"))
  val expectedResult = Result(responseHeader("no-enrolment"), HttpEntity.NoEntity)
  val userEnrollments = Enrolments(Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", "GBXXXXXXXXXX")))
  val fakeRequest = FakeRequest()
  val internalId = InternalId("internalId")

  "IsEnrolledUser" should {
    "allow the user with CDS enrolment" in {
      val isEnrolledUser = new EnrolledUserFilter()
      val user = LoggedInUser(userEnrollments, internalId, Some(Organisation), Some(Admin))
      val authenticatedRequest = AuthenticatedRequest(fakeRequest, user)
      isEnrolledUser.filter(authenticatedRequest).futureValue mustBe None
    }

    "not allow the user with no CDS enrolment" in {
      val isEnrolledUser = new EnrolledUserFilter()
      val user = LoggedInUser(Enrolments(Set.empty), internalId, Some(Organisation), Some(Admin))
      val authenticatedRequest = AuthenticatedRequest(fakeRequest, user)
      isEnrolledUser.filter(authenticatedRequest).futureValue mustBe Some(expectedResult)
    }
  }
}
