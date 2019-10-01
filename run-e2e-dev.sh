#!/usr/bin/env bash

# For testing CUSTOMS_ROSM_FRONTEND
# sm --start CUSTOMS_ROSM_FRONTEND_ALL -f (downloads and starts latest versions of all apps)
# sm --stop  CUSTOMS_ROSM_FRONTEND
# sbt -Dapplication.router=testOnlyDoNotUseInAppConf.Routes "run 9830" (start customs-rosm-frontend from local source)

sbt -Dbrowser=remote-chrome -Denvironment=dev -Dproxy.required=true endtoend:test
