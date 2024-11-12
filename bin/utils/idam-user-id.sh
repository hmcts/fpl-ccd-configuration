#!/bin/bash
## Usage: ./idam-user-id.sh [usertoken]
##
## Options:
##    - usertoken: Token to get the user id for. Can be generated with ./idam-user-token.sh.
##
## Returns a valid IDAM user id for the given token.

USER_TOKEN=${1}

curl --silent -v --show-error -X GET "${IDAM_API_BASE_URL}/details" -H "accept: application/json" -H "authorization: Bearer ${USER_TOKEN}" | jq -r .id
