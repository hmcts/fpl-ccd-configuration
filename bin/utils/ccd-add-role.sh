#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

role=${1}

userToken=$(${dir}/idam-lease-user-token.sh ${CCD_CONFIGURER_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${CCD_CONFIGURER_IMPORTER_PASSWORD:-Password12})
totp=$(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool:latest --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC})
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw $totp)

echo "Creating CCD role: ${role}"

curl --insecure --fail --show-error --silent --output /dev/null -X PUT \
  ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/api/user-role \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "'${role}'",
    "security_classification": "PUBLIC"
  }'
