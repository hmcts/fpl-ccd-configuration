#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/wait-for.sh ${SERVICE_AUTH_PROVIDER_API_BASE_URL} ${IDAM_API_BASE_URL} ${CCD_DEFINITION_STORE_API_BASE_URL} ${CCD_USER_PROFILE_API_BASE_URL}

${dir}/add-users-and-roles.sh
${dir}/import-ccd-definition.sh
