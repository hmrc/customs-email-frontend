
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

You'll need to use a Government Gateway account to access most pages as they are authenticated.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
