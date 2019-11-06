#!/usr/bin/env bash

######################
## FUNCTIONS
######################

get_user_roles() {
  docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db --username openidm --tuples-only --command "SELECT data.roles FROM managedObjects mo, LATERAL (SELECT regexp_replace(string_agg((element::json -> '_ref')::text, ','), '( *\\w*\\/)|(\")', '', 'g') AS roles FROM json_array_elements_text(mo.fullobject->'effectiveRoles') as data(element)) data WHERE mo.fullobject ->> 'userName'='${1}';" openidm
}

create_user_request() {
  response=$(
    curl --insecure --show-error --silent --output /dev/null --write-out "%{http_code}" -X POST \
      "${IDAM_API_BASE_URL:-http://localhost:5000}"/testing-support/accounts \
      -H "Content-Type: application/json" \
      -d '{
          "email":"'"${email}"'",
          "forename":"'"${email}"'",
          "surname":"'"${surname}"'",
          "password":"Password12",
          "levelOfAccess":1,
          "roles": [
            '"${rolesJson}"'
          ],
          "userGroup": {"code": "caseworker"}}
        '
  )

  echo "$response"
}

delete_user_request() {
  response=$(curl --insecure --show-error --silent --output /dev/null --write-out "%{http_code}" -X DELETE \
    "${IDAM_API_BASE_URL:-http://localhost:5000}"/testing-support/accounts/"${email}")
  echo "$response"
}

# if user exists
#   check roles ✅
#   if roles are the same do nothing ✅
#   otherwise delete ✅
#   and create user ✅ with same id ❌ <-- Apparently having the same id doesn't matter on local

recreate_user() {
  printf "%s%s\n" "Checking IDAM user: " "${email}"
  _roles=$(echo $(get_user_roles "${email}") | tr -d [:space:]) # Remove whitespace
  if [[ "$_roles" == "$rolesStr" ]]; then
    printf "%s%s\n" "Maintaining IDAM user: " "${email}"
    exit 0 # Nothing to delete and user already exists so just exit
  else
    printf "%s%s\n" "Deleting IDAM user: " "${email}"
    deleteResponse=$(delete_user_request)
    if [[ "$deleteResponse" -eq 404 ]]; then
      printf "%s%s%s\n" "User " "${email}" " doesn't exist"
      exit 1
    elif [[ "$deleteResponse" -ne 200 && "$deleteResponse" -ne 204 ]]; then
      printf "%s%s\n" "Unexpected HTTP status code from IDAM: " "${deleteResponse}"
      exit 1
    else
      printf "%s%s\n" "Recreating IDAM user: " "${email}"
      createResponse=$(create_user_request)
      if [[ "$createResponse" -eq 403 ]]; then
        printf "%s%s%s\n" "User " "${email}" " already exists"
        exit 1
      elif [[ "$createResponse" -ne 201 ]]; then
        printf "%s%s\n" "Unexpected HTTP status code from IDAM: " "${createResponse}"
        exit 1
      else
        printf "%s%s%s\n" "User " "${email}" " - updated in IDAM"
      fi
    fi
  fi
}

######################
## MAIN
######################

set -eu

if [ "${ENVIRONMENT:-local}" != "local" ]; then
  exit 0;
fi

email=${1}
rolesStr=${2}
surname=${3:-"Tester"}

IFS=',' read -ra roles <<<"${rolesStr}"

rolesJson=''
for role in "${roles[@]}"; do
  if [[ -n ${rolesJson} ]]; then
    rolesJson="${rolesJson},"
  fi
  rolesJson=${rolesJson}'{"code":"'${role}'"}'
done

printf "\n%s%s\n" "Creating IDAM user: " "${email}"

userCreationResponse=$(create_user_request)

# Unfortunately trying to create the same user throws 403, so we don't know what went wrong
if [[ $userCreationResponse -eq 403 ]]; then
  printf "%s%s%s\n" "User " "${email}" " already exists"
  recreate_user
elif [[ $userCreationResponse -ne 201 ]]; then
  printf "%s%s\n" "Unexpected HTTP status code from IDAM: " "${userCreationResponse}"
  exit 1
else
  printf "%s%s%s\n" "User " "${email}" " - added to IDAM"
fi
