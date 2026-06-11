#!/usr/bin/env bash

set -eu

MAX_ATTEMPTS="${1:-6}"
SLEEP_SECONDS="${2:-10}"

SYSTEM_UPDATE_TOKEN=$(./bin/utils/idam-lease-user-token.sh "${SYSTEM_UPDATE_USER_USERNAME}" "${SYSTEM_UPDATE_USER_PASSWORD}")
SYSTEM_UPDATE_USER_ID=$(./bin/utils/idam-user-id.sh "${SYSTEM_UPDATE_TOKEN}")

if [[ -z "${SYSTEM_UPDATE_USER_ID}" || "${SYSTEM_UPDATE_USER_ID}" == "null" ]]; then
  echo "Could not resolve system-update user id from IDAM"
  exit 1
fi

SERVICE_TOKEN=$(./bin/utils/idam-lease-service-token.sh fpl_case_service \
  "$(docker run --rm hmctsprod.azurecr.io/imported/toolbelt/oathtool --totp -b "${FPL_S2S_SECRET}")")

for ATTEMPT in $(seq 1 "${MAX_ATTEMPTS}"); do
  if ACTOR_JSON=$(curl --silent --show-error --fail \
    -H "Authorization: Bearer ${SYSTEM_UPDATE_TOKEN}" \
    -H "ServiceAuthorization: Bearer ${SERVICE_TOKEN}" \
    "${ROLE_ASSIGNMENT_URL}/am/role-assignments/actors/${SYSTEM_UPDATE_USER_ID}"); then

    HAS_CASE_ALLOCATOR=$(echo "${ACTOR_JSON}" | jq -r '
      [(.roleAssignmentResponse // [])[]
       | select(.roleName == "case-allocator" and .roleCategory == "SYSTEM" and .status == "LIVE")]
      | length > 0
    ')

    if [[ "${HAS_CASE_ALLOCATOR}" == "true" ]]; then
      echo "System-update AM case-allocator role is LIVE"
      exit 0
    fi
  fi

  echo "System-update AM role not ready (attempt ${ATTEMPT}/${MAX_ATTEMPTS}); waiting ${SLEEP_SECONDS}s"
  sleep "${SLEEP_SECONDS}"
done

echo "System-update AM role did not become available after ${MAX_ATTEMPTS} attempts"
exit 1

