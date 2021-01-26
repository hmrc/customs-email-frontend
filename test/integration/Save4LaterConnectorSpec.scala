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

package integration

import integration.stubservices.AuditService
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.customs.emailfrontend.connectors.Save4LaterConnector
import uk.gov.hmrc.http._
import utils.Constants._
import integration.stubservices.Save4LaterService._
import org.scalatest.time.{Seconds, Span}
import uk.gov.hmrc.customs.emailfrontend.model.EmailDetails
import utils.WireMockRunner

class Save4LaterConnectorSpec extends IntegrationSpec with WireMockRunner {

//  override implicit val patienceConfig = PatienceConfig(
//    timeout = scaled(Span(20, Seconds)))

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.customs-email-proxy.host" -> wireMockHost,
        "microservice.services.customs-email-proxy.port" -> wireMockPort,
        "auditing.enabled" -> true,
        "auditing.consumer.baseUri.host" -> wireMockHost,
        "auditing.consumer.baseUri.port" -> wireMockPort
      )
    )
    .build()

  private lazy val save4LaterConnector =
    app.injector.instanceOf[Save4LaterConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  before {
    resetMockServer()
  }

  override def beforeAll: Unit =
    startMockServer()

  override def afterAll: Unit =
    stopMockServer()

  "Save4LaterConnector" should {
    "return successful response with OK status and response body" in {
      stubSave4LaterGET_OK()
      save4LaterConnector
        .get[EmailDetails](id, emailKey)
        .futureValue mustBe Some(emailJson.as[EmailDetails])
//      eventually(AuditService.verifyAuditWrite())
    }

    "return successful response with NOT FOUND status" in {
      stubSave4LaterGET_NOTFOUND()
      save4LaterConnector
        .get[EmailDetails](id, emailKey)
        .futureValue mustBe None
//      eventually(AuditService.verifyAuditWrite())
    }

    "return a response with BAD REQUEST exception for Get" in {
      stubSave4LaterGET_BAD_REQUEST()

      val caught = intercept[BadRequestException] {
        await(save4LaterConnector.get[EmailDetails](id, emailKey))
      }

      caught.getMessage must startWith("Status:400")
    }

    "return successful response with Created status and response body" in {
      stubSave4LaterPUT()
      await(save4LaterConnector.put[EmailDetails](id, emailKey, emailJson)) mustBe (())
//      eventually(AuditService.verifyXAuditWrite(2))
    }

    "return a response with BAD REQUEST exception for Put" in {
      stubSave4LaterPUT_BAD_REQUEST()

      val caught = intercept[BadRequestException] {
        await(save4LaterConnector.put[User](id, emailKey, emailJson))
      }
      caught.getMessage must startWith("Status:400")
    }

    "return successful response with NoContent status for delete" in {
      stubSave4LaterDELETE()
      save4LaterConnector.delete[EmailDetails](id).futureValue mustBe (())
    }

    "return  BadRequestException with response status NOT FOUND status for unknown entry" in {
      stubSave4LaterNotFoundDELETE()
      intercept[BadRequestException] {
        await(save4LaterConnector.delete[String]("id"))
      }
    }

  }
}
