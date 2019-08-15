#!/usr/bin/env bash

set -eu

email=${1}
rolesStr=${2}
surname=${3:-"Tester"}

userToken=$($(dirname ${0})/idam-lease-user-token.sh 1 admin)

searchResponse=$(curl -k --silent --show-error --output /dev/null --write-out "%{http_code}" -H "Authorization: Bearer ${userToken}" ${IDAM_API_BASE_URL:-http://localhost:4501}/users?email=${email})

if [[ ${searchResponse} -ne 200 && ${searchResponse} -ne 404 ]]; then
  echo "The requested user search returned error: ${searchResponse}"
  exit 1
fi

if [[ ${searchResponse} -eq 200 ]]; then
  echo "User ${email} already exists in IDAM - skipping"
  exit 0
fi

echo "User ${email} - adding user to IDAM"

IFS=',' read -ra roles <<< ${rolesStr}

rolesJson=''
for role in ${roles[@]}; do
  if [[ ! -z ${rolesJson} ]] ; then
    rolesJson="${rolesJson},"
  fi
  rolesJson=${rolesJson}'{"code":"'${role}'"}'
done

curl -k --fail --show-error --silent --output /dev/null -X POST \
  ${IDAM_API_BASE_URL:-http://localhost:4501}/testing-support/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'${email}'",
    "forename": "'${email}'",
    "surname": "'${surname}'",
    "password": "Password12",
    "levelOfAccess": 1,
    "roles": [
      '${rolesJson}'
    ],
    "userGroup": {
      "code": "caseworker"
    }
  }'
