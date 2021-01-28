
# customs-email-frontend

This is a frontend application currently serving the purpose of updating the stored customs contact email address.

[[Build Status]](https://build.tax.service.gov.uk/job/EORI/view/EORI-NEW-BUILD-MONITOR/job/7.customs-email-frontend-pipeline/)


## Development

You'll need [Service Manager](https://github.com/hmrc/service-manager) to develop locally.


#### Service Manager Commands

What's running?

    sm -s

Start the required development services (make sure your service-manager-config folder is up to date)

    sm --start CUSTOMS_EMAIL_FRONTEND_DEP -f

Stop all running services

    sm --stop CUSTOMS_EMAIL_FRONTEND_DEP -f
    
#### Building
To imitate the checks ran when building run (in the root directory)

    ./precheck.sh
    
To start the app using SBT simply use the command

    sbt run

from the terminal, visit [this link](http://localhost:9898/customs-email-frontend/start) to see the app running.


#### Verify an email from another MDTP Service

If any MDTP frontend service wants to use email-frontend for email verification then you will need to check if your service name and continue url is in application.conf and if not please 
update the application.conf with your service name, continue url in the referrer-services and also the message properties file and raise a pull request. Please note the message property is derived from
the name key in the config file. In the below example it is ***customs-finance***

            referrer-services : {
              name = "customs-finance",
              continueUrl = "/customs/payment-records"
            }
            
            customs.emailfrontend.email-confirmed.redirect.info.customs-finance
            

If the service name is in `application.conf` then the url context will be `"/manage-email-cds/service/:service-name/"` for example if `customs-finance` has to use the service then the url would be `http://localhost:9898/manage-email-cds/service/customs-finance`
Also please make sure that you update below message key for english and welsh accordingly for continue url

            customs.emailfrontend.email-confirmed.redirect.info.{referrer-services.name}
            e.g. customs.emailfrontend.email-confirmed.redirect.info.customs-finance    

You'll need to use a Government Gateway account with CDS enrolment to access most pages as they are authenticated.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
