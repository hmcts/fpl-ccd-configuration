#!/usr/bin/env bash

set -e

microservice=${1}

curl -k --fail --silent -w "%{url_effective}: %{http_code} response received in %{time_total}s\n" -X POST \
  ${SERVICE_AUTH_PROVIDER_API_BASE_URL}/testing-support/lease \
  -H "Content-Type: application/json" \
  -d '{
    "microservice": "'${microservice}'"
  }'
