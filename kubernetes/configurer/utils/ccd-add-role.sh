#!/usr/bin/env bash

set -e

role=${1}

dir=$(dirname ${0})

userToken=$(${dir}/idam-lease-user-token.sh 1 ccd-import)
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw)

curl -k --fail --silent -w "%{url_effective}: %{http_code} response received in %{time_total}s\n" -X PUT \
  ${CCD_DEFINITION_STORE_API_BASE_URL}/api/user-role \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "'${role}'",
    "security_classification": "PUBLIC"
  }'
