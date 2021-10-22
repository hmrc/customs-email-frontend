
# Customs Email Frontend

This is a frontend application currently serving the purpose of updating the stored customs contact email address.

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

from the terminal, visit [this link](http://localhost:9898/manage-email-cds/start) to see the app running.

You'll need to use a Government Gateway account with CDS enrolment to access most pages as they are authenticated.
So login via the [auth-login-stub](http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9898%2Fmanage-email-cds%2Fstart) first to proceed with a journey.

#### Integrating this service into your user journeys

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

### License 
 
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
