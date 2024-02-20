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

package uk.gov.hmrc.customs.emailfrontend.model

import play.api.libs.json.{Reads, Writes}
import play.api.mvc.{PathBindable, QueryStringBindable}
import uk.gov.hmrc.customs.emailfrontend.utils.Utils.emptyString

object Ineligible extends Enumeration {
  val NoEnrolment, IsAgent, NotAdmin = Value

  implicit val reads: Reads[Ineligible.Value] = Reads.enumNameReads(Ineligible)
  implicit val writes: Writes[Ineligible.Value] = Writes.enumNameWrites

  implicit lazy val pathBindable: PathBindable[Ineligible.Value] =
    new PathBindable[Ineligible.Value] {

      override def bind(key: String, value: String): Either[String, Ineligible.Value] =
        value match {
          case "no-enrolment" => Right(NoEnrolment)
          case "is-agent" => Right(IsAgent)
          case "not-admin" => Right(NotAdmin)
          case _ => Left("invalid")
        }

      override def unbind(key: String, value: Ineligible.Value): String =
        value match {
          case NoEnrolment => "no-enrolment"
          case IsAgent => "is-agent"
          case NotAdmin => "not-admin"
          case _ => "invalid"
        }
    }

  implicit def queryBindable(implicit pathBindable: PathBindable[Ineligible.Value]
                            ): QueryStringBindable[Ineligible.Value] =

    new QueryStringBindable[Ineligible.Value] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Ineligible.Value]] =
        params.get(key).map(seq => pathBindable.bind(key, seq.headOption.getOrElse(emptyString)))

      override def unbind(key: String, value: Ineligible.Value): String =
        pathBindable.unbind(key, value)
    }
}
