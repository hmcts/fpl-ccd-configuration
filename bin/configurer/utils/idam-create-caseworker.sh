#!/usr/bin/env bash

set -eu

email=${1}
rolesStr=${2}
surname=${3:-"Tester"}

IFS=',' read -ra roles <<< ${rolesStr}

rolesJson=''
for role in ${roles[@]}; do
  if [[ ! -z ${rolesJson} ]] ; then
    rolesJson="${rolesJson},"
  fi
  rolesJson=${rolesJson}'{"code":"'${role}'"}'
done

echo -e "\nCreating IDAM user: ${email}"

userCreationResponse=$(curl --insecure --show-error --silent --output /dev/null --write-out "%{http_code}" -X POST \
  ${IDAM_API_BASE_URL:-http://localhost:5000}/testing-support/accounts \
  -H "Content-Type: application/json" \
  -d '{
  "email":"'${email}'",
  "forename":"'${email}'",
  "surname":"'${surname}'",
  "password":"'Password12'",
  "levelOfAccess":1,
  "roles": [
    '${rolesJson}'
  ],
  "userGroup": {"code": "caseworker"}}
')

# Unfortunately trying to create the same user throws 403, so we don't know what went wrong
if [[ $userCreationResponse -eq 403 ]]; then
  echo "User ${email} already exists"
elif [[ $userCreationResponse -ne 201  ]]; then
  echo "Unexpected HTTP status code from IDAM: ${userCreationResponse}"
  exit 1
else
  echo "User ${email} - added to IDAM"
fi


