#!/usr/bin/env bash

sbt -Denvironment=local -Dport=9898 endtoend:test