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

package uk.gov.hmrc.customs.emailfrontend.actions

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.Tables.Table
import org.scalatestplus.play.PlaySpec
import play.api.http.HttpEntity
import play.api.mvc.{ResponseHeader, Result}
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.customs.emailfrontend.model.{AuthenticatedRequest, InternalId, LoggedInUser}
import scala.concurrent.ExecutionContext.Implicits.global

class PermittedUserFilterSpec extends PlaySpec with ScalaFutures {

  def responseHeader(e: String) = ResponseHeader(303, Map("Location" -> s"/manage-email-cds/ineligible/$e"))
  val expectedResultNotAdmin = Result(responseHeader("not-admin"), HttpEntity.NoEntity)
  val expectedResultIsAgent = Result(responseHeader("is-agent"), HttpEntity.NoEntity)
  val eori = "GB1234556789"

  val values = Table(
    ("affinityGroup", "role", "expected", "eori"),
    (Some(Organisation), Some(User), None, eori),
    (Some(Organisation), Some(User), None, eori),
    (Some(Organisation), Some(Assistant), Some(expectedResultNotAdmin), eori),
    (Some(Agent), Some(User), Some(expectedResultIsAgent), eori),
    (Some(Individual), Some(User), None, eori),
    (Some(Individual), Some(User), None, eori)
  )
  val userEnrollments: Enrolments = Enrolments(
    Set(Enrolment("HMRC-CUS-ORG").withIdentifier("EORINumber", "GBXXXXXXXXXX"))
  )
  val fakeRequest = FakeRequest()
  val internalId = InternalId("internalId")

  "IsPermittedUser" should {
    "allow the user" in {
      forAll(values) { (affinityGroup, role, expected, eori) =>
        val user = LoggedInUser(internalId, affinityGroup, role, eori)
        val authenticatedRequest = AuthenticatedRequest(fakeRequest, user)
//        isPermittedUser.filter(authenticatedRequest).futureValue mustBe expected
      }
    }
  }
}
