#!/usr/bin/env bash

set -eu
workspace=${1}
tenant_id=${2}
product=${3}

s2sSecret=${FPL_S2S_SECRET:-AABBCCDDEEFFGGHH}

serviceToken=$($(realpath $workspace)/bin/utils/idam-lease-service-token.sh fpl_case_service \
  $(docker run --rm hmctspublic.azurecr.io/imported/toolbelt/oathtool --totp -b ${s2sSecret}))

dmnFilepath="$(realpath $workspace)/resources"

echo "${CAMUNDA_BASE_URL} import-dmn-diagram.sh line 19"

for file in $(find ${dmnFilepath} -name '*.dmn')
do
  uploadResponse=$(curl --insecure -v --silent -w "\n%{http_code}" --show-error -X POST \
    ${CAMUNDA_BASE_URL:-http://localhost:9404}/engine-rest/deployment/create \
    -H "Accept: application/json" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -F "deployment-name=$(basename ${file})" \
    -F "deploy-changed-only=true" \
    -F "deployment-source=$product" \
    ${tenant_id:+'-F' "tenant-id=$tenant_id"} \
    -F "file=@${dmnFilepath}/$(basename ${file})")

upload_http_code=$(echo "$uploadResponse" | tail -n1)
upload_response_content=$(echo "$uploadResponse" | sed '$d')

if [[ "${upload_http_code}" == '200' ]]; then
  echo "$(basename ${file}) diagram uploaded successfully (${upload_response_content})"
  continue;
fi

echo "$(basename ${file}) upload failed with http code ${upload_http_code} and response (${upload_response_content})"
continue;

done
