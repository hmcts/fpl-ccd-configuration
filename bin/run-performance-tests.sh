#!/usr/bin/env bash

set -eu

docker run --rm \
  -v $(pwd)/src/gatling/conf:/etc/gatling/conf \
  -v $(pwd)/src/gatling:/src/gatling \
  -e TEST_URL \
  -e NUMBER_OF_USERS \
  -e RAMP_UP_TIME_IN_SECONDS \
  -e IDAM_URL \
  -e IDAM_CLIENT_ID \
  -e IDAM_CLIENT_SECRET \
  -e IDAM_REDIRECT_URI \
  -e S2S_URL \
  -e S2S_SECRET \
  hmcts/gatling:3.1.1-java-11-1.0 \
  /src/gatling/run.sh
