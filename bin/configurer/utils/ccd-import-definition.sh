#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
filepath=${1}
filename=$(basename ${filepath})
uploadFilename="$(date +"%Y%m%d-%H%M%S")-${filename}"

userToken=$(${dir}/idam-lease-user-token.sh ${CCD_CONFIGURER_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${CCD_CONFIGURER_IMPORTER_PASSWORD:-Password12})
serviceToken=$(${dir}/idam-lease-service-token.sh ccd_gw $(docker run --rm toolbelt/oathtool --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}))

uploadResponse=$(curl --insecure --silent -w "\n%{http_code}" --show-error -X POST \
  ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/import \
  -H "Authorization: Bearer ${userToken}" \
  -H "ServiceAuthorization: Bearer ${serviceToken}" \
  -F "file=@${filepath};filename=${uploadFilename}")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '504' ]]; then
  for try in {1..10}
  do
    sleep 5
    echo "Checking status of ${filename} (${uploadFilename}) upload (Try ${try})"
    audit_response=$(curl --insecure --silent --show-error -X GET \
      ${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}/api/import-audits \
      -H "Authorization: Bearer ${userToken}" \
      -H "ServiceAuthorization: Bearer ${serviceToken}")

    if [[ ${audit_response} == *"${uploadFilename}"* ]]; then
      echo "${filename} (${uploadFilename}) uploaded"
      exit 0
    fi
  done
else
  if [[ "${upload_response_content}" == 'Case Definition data successfully imported' ]]; then
    echo "${filename} (${uploadFilename}) uploaded"
    exit 0
  fi
fi

echo "${filename} (${uploadFilename}) upload failed (${upload_response_content})"
exit 1;
