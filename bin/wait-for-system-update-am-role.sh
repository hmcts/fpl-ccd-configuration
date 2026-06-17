#!/usr/bin/env bash

set -eu

MAX_ATTEMPTS="${1:-6}"
SLEEP_SECONDS="${2:-10}"
TARGET_ROLE_NAME="case-allocator"
TARGET_ROLE_CATEGORY="SYSTEM"

SYSTEM_UPDATE_TOKEN=$(./bin/utils/idam-lease-user-token.sh "${SYSTEM_UPDATE_USER_USERNAME}" "${SYSTEM_UPDATE_USER_PASSWORD}")
SYSTEM_UPDATE_USER_ID=$(./bin/utils/idam-user-id.sh "${SYSTEM_UPDATE_TOKEN}")

if [[ -z "${SYSTEM_UPDATE_USER_ID}" || "${SYSTEM_UPDATE_USER_ID}" == "null" ]]; then
  echo "Could not resolve system-update user id from IDAM"
  exit 1
fi

SERVICE_TOKEN=$(./bin/utils/idam-lease-service-token.sh fpl_case_service \
  "$(docker run --rm hmctsprod.azurecr.io/imported/toolbelt/oathtool --totp -b "${FPL_S2S_SECRET}")")

for ATTEMPT in $(seq 1 "${MAX_ATTEMPTS}"); do
  QUERY_BODY=$(jq -n \
    --arg actorId "${SYSTEM_UPDATE_USER_ID}" \
    --arg roleName "${TARGET_ROLE_NAME}" \
    '{"actorId":[$actorId],"roleName":[$roleName],"roleType":["ORGANISATION"]}')

  if QUERY_JSON=$(curl --silent --show-error --fail \
    -H "Authorization: Bearer ${SYSTEM_UPDATE_TOKEN}" \
    -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
    -H "Content-Type: application/json" \
    -X POST \
    -d "${QUERY_BODY}" \
    "${ROLE_ASSIGNMENT_URL}/am/role-assignments/query"); then
    MATCHING_TOTAL="$(echo "${QUERY_JSON}" | jq -r \
      --arg roleName "${TARGET_ROLE_NAME}" \
      --arg roleCategory "${TARGET_ROLE_CATEGORY}" '
      [(.roleAssignmentResponse // [])[]
       | select(.roleName == $roleName and ((.roleCategory // "") | ascii_upcase) == $roleCategory)
      ]
      | length')"

    if [[ "${MATCHING_TOTAL}" -gt 0 ]]; then
      echo "System-update AM ${TARGET_ROLE_NAME} role is available (matches=${MATCHING_TOTAL})"
      exit 0
    fi

    echo "System-update AM ${TARGET_ROLE_NAME} role not ready (attempt ${ATTEMPT}/${MAX_ATTEMPTS}): matches=${MATCHING_TOTAL}"
  else
    echo "AM query failed (attempt ${ATTEMPT}/${MAX_ATTEMPTS})"
  fi

  echo "Waiting ${SLEEP_SECONDS}s before retry"
  sleep "${SLEEP_SECONDS}"
done

echo "System-update AM ${TARGET_ROLE_NAME} role did not become available after ${MAX_ATTEMPTS} attempts"
exit 1

