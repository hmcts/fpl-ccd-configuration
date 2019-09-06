#!/usr/bin/env bash

set -eu

docker run --rm \
  -v $(pwd)/src/gatling/conf:/etc/gatling/conf \
  -v $(pwd)/src/gatling:/src/gatling \
  -e SERVICE_BASE_URL=${TEST_URL:-http://docker.for.mac.localhost:4000} \
  hmcts/gatling:3.1.1-java-11-1.0 \
  /src/gatling/run.sh