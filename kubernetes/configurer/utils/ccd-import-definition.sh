#!/usr/bin/env bash

set -e

dir=$(dirname ${0})

userToken=$(${dir}/idam-lease-user-token.sh 1 ccd-import)
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw)

curl -k --fail --show-error --silent -X POST \
  ${CCD_DEFINITION_STORE_API_BASE_URL}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F file=@${1}
