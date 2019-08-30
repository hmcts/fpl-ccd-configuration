#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

userToken=$(${dir}/idam-lease-user-token.sh ${CCD_CONFIGURER_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${CCD_CONFIGURER_IMPORTER_PASSWORD:-Password12})
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw $(docker run --rm toolbelt/oathtool --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}))

response=$(curl --insecure --silent --show-error -X POST \
  ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F file=@${1})

echo ${response}

if [[ "${response}" != 'Case Definition data successfully imported' ]]; then
  exit 1
fi
