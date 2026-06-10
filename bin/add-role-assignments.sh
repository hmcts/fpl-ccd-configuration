#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
skip_system_update_am_role_seeding=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-system-update-am-role-seeding)
      skip_system_update_am_role_seeding=true
      shift
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 1
      ;;
  esac
done

jq -c '(.[])' service/src/cftlib/resources/cftlib-am-role-assignments.json | while read user; do
  email=$(jq -r '.email' <<< $user)
  idamId=$(jq -r '.id' <<< $user)
  password=${SYSTEM_UPDATE_USER_PASSWORD}

  if [[ "$skip_system_update_am_role_seeding" == "true" && "$email" == "fpl-system-update@mailnesia.com" ]]; then
    echo "Skipping system-update AM role seeding in preview"
    continue
  fi

  if [[ $email == *"ejudiciary"* ]]; then
    password=${E2E_TEST_JUDGE_PASSWORD}
  fi

  jq -c '(.roleAssignments[])' <<< $user | while read assignment; do
    roleName=$(jq -r '.roleName' <<< $assignment)
    roleCategory=$(jq -r '.roleCategory' <<< $assignment)
    classification=$(jq -r '.classification' <<< $assignment)
    grantType=$(jq -r '.grantType' <<< $assignment)
    readOnly=$(jq -r '.readOnly' <<< $assignment)
    attributes=$(jq -r '.attributes | tostring' <<< $assignment)

    authorisations=$(jq -r '.authorisations | tostring' <<< $assignment)

    echo "Creating '${roleName}' assignment for user ${email}"
    ${dir}/utils/organisational-role-assignment.sh $email $password $classification $roleName $attributes $roleCategory $authorisations $grantType
  done
  echo
done
