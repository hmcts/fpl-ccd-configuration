#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
filepath=${1}
filename=$(basename ${filepath})
uploadFilename="$(date +"%Y%m%d-%H%M%S")-${filename}"
importJobId=$(uuidgen | tr '[:upper:]' '[:lower:]')

echo filepath = $filepath
echo "Import Job ID: ${importJobId}"

# Get userToken
if [ -z "${USER_TOKEN:-}" ]; then
  echo "Getting userToken"
  userToken=$(${dir}/utils/idam-lease-user-token.sh ${CCD_CONFIGURER_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net} ${CCD_CONFIGURER_IMPORTER_PASSWORD:-Password12})
  echo ">>> Got userToken"
else
  echo ">>> Couldn't get userToken - Using cached userToken"
  userToken=${USER_TOKEN}
fi

# Get serviceToken
if [ -z "${SERVICE_TOKEN:-}" ]; then
  echo "Getting serviceToken"
  serviceToken=$(${dir}/utils/idam-lease-service-token.sh ccd_gw $(docker run --rm hmctsprod.azurecr.io/imported/toolbelt/oathtool:latest --totp -b ${CCD_API_GATEWAY_S2S_SECRET:-AAAAAAAAAAAAAAAC}))
  echo ">>> Got serviceToken"
else
  echo ">>> Couldn't get serviceToken - Using cached serviceToken"
  serviceToken=${SERVICE_TOKEN}
fi

#version="n/a"
#newVersion="n/a"
ccdDefinitionStoreUrl=${CCD_DEFINITION_STORE_API_BASE_URL:-http://localhost:4451}
echo "ccdDefinitionStoreUrl = ${ccdDefinitionStoreUrl}"

# Poll import status
poll_import_status() {
  for try in {1..10}; do
    if [[ "$ENVIRONMENT" == "preview" ]]; then
      sleep 10
    else
      sleep 5
    fi

    echo "Checking import job ${importJobId} status (Try ${try})"

    jobResponse=$(curl --insecure --silent --show-error \
      -X GET \
      "${ccdDefinitionStoreUrl}/import-jobs/${importJobId}" \
      -H "Authorization: Bearer ${userToken}" \
      -H "ServiceAuthorization: Bearer ${serviceToken}" || true)

    if [[ "${jobResponse}" == *"COMPLETED"* ]]; then
      echo ">>> ${filename} (${uploadFilename}) uploaded"
      exit 0
    fi

    if [[ "${jobResponse}" == *"FAILED"* ]]; then
      echo ">>> ${filename} (${uploadFilename}) upload failed: ${jobResponse}"
      exit 1
    fi

    if [[ "${jobResponse}" == *"EXPIRED"* ]]; then
      echo ">>> ${filename} (${uploadFilename}) upload expired: ${jobResponse}"
      exit 1
    fi
  done

  echo ">>> ${filename} (${uploadFilename}) upload status could not be confirmed"
  exit 1
}

# Get current case version if in Preview
#if [[ "$ENVIRONMENT" == "preview" ]]; then
#  version=$(curl --insecure --silent --show-error -X GET \
#    "${ccdDefinitionStoreUrl}/api/data/case-type/CARE_SUPERVISION_EPO/version" \
#    -H "Authorization: Bearer ${userToken}" \
#    -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')
#
#  echo "Current CCD definition version is ${version}"
#fi

useShutteredCaseDef=${USE_SHUTTERED_CASE_DEF:-0}
if [[ "$useShutteredCaseDef" == "1" ]]; then
  echo "Using Shuttered Case Definition File"
  uploadFilename="$(printf '%s' "${uploadFilename}" | sed 's/\.xlsx/-shuttered\.xlsx/')"
fi

max_upload_attempts=3
upload_retry_delay=5
attempt=1
upload_curl_failed=false

# Attempt to upload case def file
while true; do
  echo "Uploading ${filename} (${uploadFilename}) - attempt ${attempt}/${max_upload_attempts}"

  if uploadResponse=$(curl --insecure --silent -w "\n%{http_code}" --show-error -X POST \
    "${ccdDefinitionStoreUrl}/import" \
    -H "Authorization: Bearer ${userToken}" \
    -H "ServiceAuthorization: Bearer ${serviceToken}" \
    -H "X-Import-Job-Id: ${importJobId}" \
    -F "file=@${filepath};filename=${uploadFilename}"); then

    upload_http_code=$(echo "$uploadResponse" | tail -n1)
    upload_response_content=$(echo "$uploadResponse" | sed '$d')
  else
    upload_curl_failed=true
    echo ">>> Import request encountered a connection error technical error"
    break
  fi

  # Skip audit check on Preview b/c import takes 60s but infra cuts of at 30s
#  if [[ "$ENVIRONMENT" == "preview" ]] && [[ "$upload_http_code" != "201" ]]; then
#    echo ">>> Upload on Preview didn't return HTTP 201. Waiting 45s and then verifying the version has changed..."
#    sleep 45
#
#    newVersion=$(curl --insecure --silent --show-error -X GET \
#      "${ccdDefinitionStoreUrl}/api/data/case-type/CARE_SUPERVISION_EPO/version" \
#      -H "Authorization: Bearer ${userToken}" \
#      -H "ServiceAuthorization: Bearer ${serviceToken}" || echo 'bypass-if-error')
#
#      echo "Current version is ${newVersion}"
#      if [[ "$newVersion" == "$version" ]]; then
#        echo ">>> CCD definition version for ${filename} (${uploadFilename}) has not changed - definition import failed"
#        exit 1
#      fi
#
#    echo ">>> CCD definition version for ${filename} (${uploadFilename}) has changed - definition imported successfully"
#    exit 0
#  fi

  # If gateway timeout, audit imports to check if import was successful
#  if [[ "${upload_http_code}" == '504' ]]; then
#    for try in {1..20}; do
#      echo "Checking status of ${filename} (${uploadFilename}) upload (Try ${try})"
#      sleep 5
#      audit_response=$(curl --insecure --silent --show-error -X GET \
#        "${ccdDefinitionStoreUrl}/api/import-audits" \
#        -H "Authorization: Bearer ${userToken}" \
#        -H "ServiceAuthorization: Bearer ${serviceToken}")
#
#      if [[ ${audit_response} == *"${uploadFilename}"* ]]; then
#        echo ">>> ${filename} (${uploadFilename}) uploaded"
#        exit 0
#      fi
#    done
#  else
#    if [[ "${upload_response_content}" == 'Case Definition data successfully imported' ]]; then
#      echo ">>> ${filename} (${uploadFilename}) uploaded"
#      exit 0
#    fi
#  fi

  # Retry if the HTTP code is 409 (importJobId Conflict - ID already exists)
  if [[ "${upload_http_code}" != "409" ]]; then
    break
  fi

  # Retry if the max_upload_attempts has not been exceeded
  if [[ "${attempt}" -ge "${max_upload_attempts}" ]]; then
    break
  fi

  echo ">>> Upload returned HTTP 409. Retrying in ${upload_retry_delay} seconds..."
  sleep "${upload_retry_delay}"
  attempt=$((attempt + 1))
done

# Check if import successful when cURL fails rather than /import endpoint
if [[ "${upload_curl_failed}" == "true" ]]; then
  echo "Checking import job: ${importJobId}"
  poll_import_status
fi

if [[ "${upload_http_code}" =~ ^2[0-9][0-9]$ ]]; then
  echo ">>> ${filename} (${uploadFilename}) uploaded"
  exit 0
fi

if [[ "${upload_http_code}" == "409" ]]; then
  echo ">>> ${filename} (${uploadFilename}) upload failed after ${attempt} attempts: HTTP 409 (${upload_response_content})"
  exit 1
fi

if [[ "${upload_http_code}" =~ ^4[0-9][0-9]$ ]]; then
  echo ">>> Client error when uploading ${filename} (${uploadFilename}): HTTP ${upload_http_code} (${upload_response_content})"
  exit 1
fi

if [[ "${upload_http_code}" =~ ^5[0-9][0-9]$ ]]; then
  echo ">>> Server error when uploading ${filename} (${uploadFilename}): HTTP ${upload_http_code} (${upload_response_content})"
  echo "Checking import job: ${importJobId}"
  poll_import_status
fi

echo "Import returned HTTP ${upload_http_code} - checking job ${importJobId}"
poll_import_status



# Can i remove the Preview & !201 check
  # Skips /import-audits check to avoid timeout on preview
# Can i remove the 504 check
  # /import-audits to check if import succeeded despite server error
# Can i remove the ^5[0-9][0-9] check
  # Outputs a different log message and invokes poll_import_status
#
# /import creates and updates a job record in DB
# /import-jobs/{id} reads that record & returns its stored status in a 200 response
#
# Status from /import-jobs/{id} not based on http code from /import
# Based on exceptions that /import raises
