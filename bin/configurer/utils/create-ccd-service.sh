#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

LABEL="${1:-ccd_gateway}"
SELF_REG="${2:-true}"
CLIENT_ID="${3:-ccd_gateway}"
CLIENT_SECRET="${4:-ccd_gateway_secret}"
REDIRECT_URLS="${5:-[\"http://localhost:3451/oauth2redirect\"]}"

apiToken=$(${dir}/idam-authenticate.sh "${IDAM_ADMIN_USER:-idamOwner@hmcts.net}" "${IDAM_ADMIN_PASSWORD:-Ref0rmIsFun}")

echo -e "\nCreating service with:\nLabel: ${LABEL}\nClient ID: ${CLIENT_ID}\nClient Secret: ${CLIENT_SECRET}\nRedirect URL: ${REDIRECT_URLS}\n"

STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST -H 'Content-Type: application/json' -H "Authorization: AdminApiAuthToken ${apiToken}" \
  http://localhost:5000/services \
  -d '{
    "allowedRoles": [],
    "description": "'${LABEL}'",
    "label": "'${LABEL}'",
    "oauth2ClientId": "'${CLIENT_ID}'",
    "oauth2ClientSecret": "'${CLIENT_SECRET}'",
    "oauth2RedirectUris": '${REDIRECT_URLS}',
    "oauth2Scope": "openid profile authorities acr roles search-user",
    "selfRegistrationAllowed": true
}')

if [ $STATUS -eq 201 ]; then
  echo "Service created sucessfully"
elif [ $STATUS -eq 409 ]; then
  echo "Service already exists!"
else
  echo "ERROR: HTTPCODE = $STATUS"
  exit 1
fi
