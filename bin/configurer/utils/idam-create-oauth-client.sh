#!/usr/bin/env bash

set -eu

id=${1}
clientSecret=${2}
allowedRedirectUris=${3}

userToken=$($(dirname ${0})/idam-lease-user-token.sh 1 sysop)

searchResponse=$(curl -k --silent --show-error --output /dev/null --write-out "%{http_code}" -H "Authorization: Bearer ${userToken}" ${IDAM_API_BASE_URL:-http://localhost:4501}/clients/${id})

if [[ ${searchResponse} -ne 200 && ${searchResponse} -ne 404 ]]; then
  echo "The requested user search returned error: ${searchResponse}"
  exit 1
fi

if [[ ${searchResponse} -eq 200 ]]; then
  echo "Client '${id}' already exists in IDAM - skipping"
  exit 0
fi

echo "Client '${id}' - adding client to IDAM"

curl -k --fail --show-error --silent --output /dev/null -X POST \
  ${IDAM_API_BASE_URL:-http://localhost:4501}/clients \
  -H "Authorization: Bearer ${userToken}" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "'${id}'",
    "clientSecret": "'${clientSecret}'",
    "allowedRedirectUris": "'${allowedRedirectUris}'"
  }'
