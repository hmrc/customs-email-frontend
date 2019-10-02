#!/usr/bin/env bash

sbt -Dbrowser=remote-chrome -Denvironment=dev -Dproxy.required=true endtoend:test
