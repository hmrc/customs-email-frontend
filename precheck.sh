#!/usr/bin/env bash

sbt clean coverage test test:scalafmt coverageReport
