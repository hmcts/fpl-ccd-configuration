#!/usr/bin/env bash

set -eu

if [[ "${IDAM_VERSION:-tactical}" == 'tactical' ]]; then
  id=${1}
  role=${2}

  curl -k --fail --show-error --silent -X POST \
    ${IDAM_API_BASE_URL:-http://localhost:4501}/testing-support/lease \
    -Fid=${id} \
    -Frole=${role}
else
  importerUsername=${CCD_CONFIGURER_IMPORTER_USERNAME}
  importerPassword=${CCD_CONFIGURER_IMPORTER_PASSWORD}
  clientId=${CCD_CONFIGURER_IDAM_CLIENT_ID}
  clientSecret=${CCD_CONFIGURER_IDAM_CLIENT_SECRET}
  redirectUri=https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect

  code=$(curl -k --fail --show-error --silent -X POST -u "${importerUsername}:${importerPassword}" "${IDAM_API_BASE_URL}/oauth2/authorize?redirect_uri=${redirectUri}&response_type=code&client_id=${clientId}" -d "" | jq -r .code)

  curl -k --fail --show-error --silent -X POST -H "Content-Type: application/x-www-form-urlencoded" -u "${clientId}:${clientSecret}" "${IDAM_API_BASE_URL}/oauth2/token?code=${code}&redirect_uri=${redirectUri}&grant_type=authorization_code" -d "" | jq -r .access_token
fi
