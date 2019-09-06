#!/usr/bin/env bash

set -eu

export JAVA_HOME=''

/etc/gatling/bin/gatling.sh \
  --run-description 'FPL Case Service API performance tests' \
  --simulations-folder /src/gatling/simulations \
  --resources-folder /src/gatling/resources