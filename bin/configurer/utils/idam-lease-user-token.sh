#!/usr/bin/env bash

set -eu

importerUsername=${CCD_CONFIGURER_IMPORTER_USERNAME:-ccd.docker.default@hmcts.net}
importerPassword=${CCD_CONFIGURER_IMPORTER_PASSWORD:-Password12}
clientSecret=${CCD_API_GATEWAY_IDAM_CLIENT_SECRET:-ccd_gateway_secret}
redirectUri=${CCD_IDAM_REDIRECT_URL:-http://localhost:3451/oauth2redirect}

code=$(curl -k --fail --show-error --silent -X POST -u "${importerUsername}:${importerPassword}" "${IDAM_API_BASE_URL:-http://localhost:5000}/oauth2/authorize?redirect_uri=${redirectUri}&response_type=code&client_id=ccd_gateway" -d "" | docker run --rm --interactive stedolan/jq -r .code)

curl -k --fail --show-error --silent -X POST -H "Content-Type: application/x-www-form-urlencoded" -u "ccd_gateway:${clientSecret}" "${IDAM_API_BASE_URL:-http://localhost:5000}/oauth2/token?code=${code}&redirect_uri=${redirectUri}&grant_type=authorization_code" -d "" | docker run --rm --interactive stedolan/jq -r .access_token
