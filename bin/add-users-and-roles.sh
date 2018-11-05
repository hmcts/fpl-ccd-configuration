#!/usr/bin/env bash

set -e

export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://localhost:4502
export IDAM_API_BASE_URL=http://localhost:4501
export CCD_DEFINITION_STORE_API_BASE_URL=http://localhost:4451

dir=$(dirname ${0})

${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh local-authority@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-localAuthority
${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh hmcts-admin@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-courtadmin
${dir}/../kubernetes/configurer/utils/ccd-add-role.sh caseworker-publiclaw-localAuthority
${dir}/../kubernetes/configurer/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin
