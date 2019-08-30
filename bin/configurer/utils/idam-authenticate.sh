#!/bin/sh

set -eu

USERNAME=$1
PASSWORD=$2

curl --silent --show-error --header 'Content-Type: application/x-www-form-urlencoded' -H 'Accept: application/json' -d "username=${USERNAME}&password=${PASSWORD}" "${IDAM_API_BASE_URL:-http://localhost:5000}/loginUser" | docker run --rm --interactive stedolan/jq -r .api_auth_token
