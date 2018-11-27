#!/usr/bin/env bash

set -eu -x

microservice=${1}
oneTimePassword=${2}

curl -k --fail --show-error --silent -X POST \
  ${SERVICE_AUTH_PROVIDER_API_BASE_URL}/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'",
    "oneTimePassword": "'${oneTimePassword}'"
  }'
