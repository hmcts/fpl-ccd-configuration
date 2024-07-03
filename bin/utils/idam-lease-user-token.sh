#!/usr/bin/env bash

set -eu

username=${1}
password=${2}
clientSecret=${CCD_API_GATEWAY_IDAM_CLIENT_SECRET:-ccd_gateway_secret}
redirectUri=${CCD_IDAM_REDIRECT_URL:-http://localhost:3451/oauth2redirect}

code=$(curl --insecure --fail --show-error --silent -X POST --user "${username}:${password}" "${IDAM_API_BASE_URL:-http://localhost:5000}/oauth2/authorize?redirect_uri=${redirectUri}&response_type=code&client_id=ccd_gateway" -d "" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .code)

curl --insecure --fail --show-error --silent -X POST -H "Content-Type: application/x-www-form-urlencoded" --user "ccd_gateway:${clientSecret}" "${IDAM_API_BASE_URL:-http://localhost:5000}/oauth2/token?code=${code}&redirect_uri=${redirectUri}&grant_type=authorization_code" -d "" | docker run --rm --interactive ghcr.io/jqlang/jq:latest -r .access_token
