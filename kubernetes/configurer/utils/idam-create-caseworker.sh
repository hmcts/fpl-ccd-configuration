#!/usr/bin/env bash

set -e

email=${1}
rolesStr=${2}

searchResponseStatusCode=$(curl -k --silent --output /dev/null --write-out "%{http_code}" ${IDAM_API_BASE_URL}/users?email=${email})

if [ ${searchResponseStatusCode} -eq 200 ]; then
  exit 0
fi

IFS=',' read -ra roles <<< ${rolesStr}

for role in ${roles[@]}; do
  if [ ! -z ${rolesJson} ] ; then
    rolesJson="${rolesJson},"
  fi
  rolesJson=${rolesJson}'{"code":"'${role}'"}'
done

curl -k --fail --silent --write-out "%{url_effective}: %{http_code} response received in %{time_total}s\n" -X POST \
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
