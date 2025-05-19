
# Customs Email Frontend

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Coverage](https://img.shields.io/badge/test_coverage-90-green.svg)](/target/scala-2.11/scoverage-report/index.html) [![Accessibility](https://img.shields.io/badge/WCAG2.2-AA-purple.svg)](https://www.gov.uk/service-manual/helping-people-to-use-your-service/understanding-wcag)

This is a frontend microservice currently serving the purpose of updating the stored customs contact email address.
This service is also known as 'Manage email service' and also used as a standalone service.

This service is built following GDS standards to [WCAG 2.2 AA](https://www.gov.uk/service-manual/helping-people-to-use-your-service/understanding-wcag)

We use the [GOV.UK design system](https://design-system.service.gov.uk/) to ensure consistency and compliance through the project

This application lives in the "public" zone. It integrates with:

## Running the service locally

### Runtime dependencies

The application has the following runtime dependencies:

* `AUTH`
* `AUTH_LOGIN_STUB`
* `AUTH_LOGIN_API`
* `BAS_GATEWAY_FRONTEND`
* `ACCESSIBILITY_STATEMENT_FRONTEND`
* `CENTRALISED_AUTHORISATION_SERVER`
* `DATASTREAM`
* `IDENTITY_VERIFICATION`
* `USER_DETAILS`
* `CONTACT_FRONTEND`
* `TRACKING_CONSENT_FRONTEND`
* `AUTHENTICATOR`
* `CUSTOMS_EMAIL_PROXY`
* `CUSTOMS_DATA_STORE`
* `CUSTOMS_EMAIL_STUBS`

Default service port on local - 9898

The easiest way to get started with these is via the service manager CLI - you can find the installation guide [here](https://docs.tax.service.gov.uk/mdtp-handbook/documentation/developer-set-up/set-up-service-manager.html)

| Command                                     | Description                                      |
|---------------------------------------------|--------------------------------------------------|
| `sm2 --start CUSTOMS_EMAIL_FRONTEND_ALL -f` | Runs all dependencies                            |
| `sm2 -s`                                    | Shows running services                           |
| `sm2 --stop CUSTOMS_EMAIL_FRONTEND`         | Stop the micro service                           |
| `sbt run` or `sbt "run 9898"`               | (from root dir) starts the service on port  9898 |
| `sbt "start -Dhttp.port=9898"`              | Run service in 'PROD mode'                       |

### Login enrolments

You'll need to use a Government Gateway account with CDS enrolment to access most pages as they are authenticated.

Local access - Login via the [auth-login-stub](http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9898%2Fmanage-email-cds%2Fstart)
first to proceed with a journey.

Acess on Dev/QA/Staging - Login via the [auth-login-stub](https://www.<env_name>.tax.service.gov.uk/auth-login-stub/gg-sign-in?continue=https://www.<env_name>.tax.service.gov.uk/manage-email-cds/start)
first to proceed with a journey.

The service is also accessed from other MIDVA's frontend and backend microservice to check the primary email address status
This service is also used to update the primary email address using 'Contact details' link on MIDVA home page

| Enrolment Key	 | Identifier Name | Identifier Value |
|----------------|-----------------|------------------|
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982000` |
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982001` |

## Running tests and test coverage

There is just one test source tree in the `test` folder. Use `sbt test` to run them.

To get a unit test coverage report, you can run `sbt clean coverage test coverageReport`,
then open the resulting coverage report `target/scala-3.x.x/scoverage-report/index.html` in a web browser.

The minimum requirement for test coverage is 90%. Builds will fail when the project drops below this threshold.

## Available Routes

You can find a list of microservice specific routes here - `/conf/app.routes`

| Path                                  | Description                              |
|---------------------------------------|------------------------------------------|
| GET  /manage-email-cds/start          | Start of journey for managing CDS email  |
| GET /manage-email-cds/service/:name   | Used via other HMRC services as referrer |

# Integrating this service into your user journeys

If another MDTP frontend service wants to use customs-email-frontend for email updating or verification then you will need to 
* add your service 'name' and 'continueUrl' keys into the application.conf
* define the link text offered to the user to return to your service in the message properties files (please note the message property key's suffix should match your service 'name' value defined in the application.conf file).
* add new tests to `unit.views.EmailConfirmedViewSpec` and `unit.config.AppConfigSpec` 
* create a new branch and raise a pull request once you have finished. 

Here is an example for the ***customs-finance*** service

**application.conf**

            referrer-services : {
              name = "customs-finance",
              continueUrl = "/customs/payment-records"
            }
            
**messages.en**

            customs.emailfrontend.email-confirmed.redirect.info.customs-finance=You can now continue to <a href="{0}">Get your import VAT and duty adjustment statements.</a>

Please make sure that you update the message keys for both english and welsh languages!
            
To provide a link from your service to the customs-email-frontend you need to create a url with the following pattern:

    /manage-email-cds/service/:service-name/

Where `:service-name` equals your service 'name' value defined in application.conf. 
So for example for ***customs-finance***  the url would be `http://localhost:9898/manage-email-cds/service/customs-finance`

## Feature Switches
 Not applicable

## Scalastyle, scala formatting and run all checks

### Scalastyle checks
| Command               | Description                                                                |
|-----------------------|----------------------------------------------------------------------------|
| `sbt scalastyle`      | Runs scala style checks based on scalastyle-config.xml                     |                                                     |
| `sbt Test/scalastyle` | Runs scala style checks for unit tests based on test-scalastyle-config.xml |

### Scala format
| Command                | Description                                 |
|------------------------|---------------------------------------------|
| `sbt scalafmtCheckAll` | Scala Format checks based on .scalafmt.conf |                                                     |
| `sbt scalafmtAll`      | Formats the code based on .scalafmt.conf    |
| `sbt scalafmtOnly`     | Formats specified files listed              |

### Run all checks
This is a sbt command alias specific to this project. It will run a scala style check, run unit tests, run integration tests and produce a coverage report:
> `sbt runAllChecks`

## Helpful commands

| Command                                       | Description                                                                                                                                |
|-----------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `sbt runAllChecks`                            | Runs all standard code checks                                                                                                              |
| `sbt clean`                                   | Cleans code                                                                                                                                |
| `sbt compile`                                 | Compiles the code                                                                                                                          |
| `sbt test`                                    | Runs unit tests                                                                                                                            |
| `sbt it/test`                                 | Runs integration tests                                                                                                                     |
| `sbt scalafmtCheckAll`                        | Runs code formatting checks based on .scalafmt.conf                                                                                        |
| `sbt scalastyle`                              | Runs scala style checks based on scalastyle-config.xml                                                                                     |
| `sbt Test/scalastyle`                         | Runs scala style checks for unit tests based on test-scalastyle-config.xml                                                                 |
| `sbt coverageReport`                          | Produces a code coverage report                                                                                                            |
| `sbt "test:testOnly *TEST_FILE_NAME*"`        | Runs tests for a single file                                                                                                               |
| `sbt clean coverage test coverageReport`      | Runs the unit test with enabled coverage and generates coverage report that you can find in target/scala-3.x.x/scoverage-report/index.html |
| `sbt "run -Dfeatures.some-feature-name=true"` | Enables a feature locally without risking exposure                                                                                         |
