/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.customs.emailfrontend.model

import uk.gov.hmrc.auth.core.{AffinityGroup, CredentialRole, EnrolmentIdentifier}

case class LoggedInUser(internalId: InternalId,
                        affinityGroup: Option[AffinityGroup],
                        credentialRole: Option[CredentialRole],
                        eori: String)

case class Eori(id: String)

object Eori {
  def apply(identifier: EnrolmentIdentifier) = new Eori(identifier.value)
}

case class InternalId(id: String)
