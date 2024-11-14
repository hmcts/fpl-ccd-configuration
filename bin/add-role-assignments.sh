#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

jq -c '(.[])' service/src/cftlib/resources/cftlib-am-role-assignments.json | while read user; do
  email=$(jq -r '.email' <<< $user)
  idamId=$(jq -r '.id' <<< $user)

  jq -c '(.roleAssignments[])' <<< $user | while read assignment; do
    roleName=$(jq -r '.roleName' <<< $assignment)
    roleCategory=$(jq -r '.roleCategory' <<< $assignment)
    classification=$(jq -r '.classification' <<< $assignment)
    grantType=$(jq -r '.grantType' <<< $assignment)
    readOnly=$(jq -r '.readOnly' <<< $assignment)
    attributes=$(jq -r '.attributes | tostring' <<< $assignment)

    authorisations=$(jq -r 'if .authorisations | length > 0 then "'"'"'{" + (.authorisations | join(",")) + "}'"'"'" else null end' <<< $assignment)

    echo "Creating '${roleName}' assignment for user ${email}"
    ${dir}/utils/organisational-role-assignment.sh $email ${SYSTEM_UPDATE_USER_PASSWORD} $classification $roleName $attributes $roleCategory $authorisations $grantType
  done
  echo
done
