#!/usr/bin/env bash

# For testing CUSTOMS_ROSM_FRONTEND
# sm --start CUSTOMS_ROSM_FRONTEND_ALL -f (downloads and starts latest versions of all apps)

# mongo --quiet --eval 'db.customs-frontend.dropDatabase()})'

sbt -Denvironment=local -Dport=9898 endtoend:test