#!/usr/bin/env bash

set -e

microservice=${1}

curl -k --fail --show-error -X POST \
  ${SERVICE_AUTH_PROVIDER_API_BASE_URL}/testing-support/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'"
  }'
