#!/usr/bin/env bash

set -e

max_health_check_attempts=30

function checkHealth {
  for service_base_url in ${SERVICE_AUTH_PROVIDER_API_BASE_URL} ${IDAM_API_BASE_URL} ${CCD_DEFINITION_STORE_API_BASE_URL}; do
    curl -k --fail --silent --output /dev/null --head ${service_base_url}/health
    if [ $? -ne 0 ]; then
      exit 1
    fi
  done
}

until $(checkHealth); do
  current_health_check_attempt=$((${current_health_check_attempt:-1} + 1))

  if [ ${current_health_check_attempt} -gt ${max_health_check_attempts} ]; then
    echo -e "\nMax number of attempts reached"
    exit 1
  fi

  if [ ${current_health_check_attempt} -eq 2 ]; then
    printf 'Awaiting healthy services'
  else
    printf '.'
  fi

  sleep 10
done

dir=$(dirname ${0})

${dir}/utils/idam-create-caseworker.sh local-authority@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-localAuthority
${dir}/utils/idam-create-caseworker.sh hmcts-admin@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-courtadmin
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-localAuthority
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin

${dir}/utils/fpl-process-definition.sh
${dir}/utils/ccd-import-definition.sh ${dir}/../../ccd-definition.xlsx
