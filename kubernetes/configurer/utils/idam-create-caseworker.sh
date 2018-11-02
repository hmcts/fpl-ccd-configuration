#!/usr/bin/env bash

set -e

email=${1}
rolesStr=${2}

searchResponse=$(curl -k --silent --show-error --output /dev/null --write-out "%{http_code}" ${IDAM_API_BASE_URL}/users?email=${email})

if [ ${searchResponse} -eq 200 ]; then
  exit 0
fi

IFS=',' read -ra roles <<< ${rolesStr}

for role in ${roles[@]}; do
  if [ ! -z ${rolesJson} ] ; then
    rolesJson="${rolesJson},"
  fi
  rolesJson=${rolesJson}'{"code":"'${role}'"}'
done

curl -k --fail --show-error --silent -X POST \
  ${IDAM_API_BASE_URL}/testing-support/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "'${email}'",
    "forename": "Tester",
    "surname": "Tester",
    "password": "Password12",
    "levelOfAccess": 1,
    "roles": [
      '${rolesJson}'
    ],
    "userGroup": {
      "code": "caseworker"
    }
  }'
