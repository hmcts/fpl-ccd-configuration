#!/usr/bin/env bash

set -eu

role=${1}

dir=$(dirname ${0})

userToken=$(${dir}/idam-lease-user-token.sh 1 ccd-import)
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw $(docker run --rm toolbelt/oathtool --totp -b ${CCD_CONFIGURER_S2S_SECRET}))

echo "Role ${role} - adding"

curl -k --fail --show-error --silent --output /dev/null -X PUT \
  ${CCD_DEFINITION_STORE_API_BASE_URL}/api/user-role \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "'${role}'",
    "security_classification": "PUBLIC"
  }'
