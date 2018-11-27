#!/usr/bin/env bash

set -eu

export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://localhost:4502
export IDAM_API_BASE_URL=http://localhost:4501
export CCD_DEFINITION_STORE_API_BASE_URL=http://localhost:4451

export CCD_CONFIGURER_S2S_SECRET=AAAAAAAAAAAAAAAC

dir=$(dirname ${0})

${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh damian@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor
${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh kurt@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor
${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh james@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor
${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh sam@hillingdon.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor
${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh siva@hillingdon.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor
${dir}/../kubernetes/configurer/utils/idam-create-caseworker.sh hmcts-admin@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-courtadmin
${dir}/../kubernetes/configurer/utils/ccd-add-role.sh caseworker-publiclaw-solicitor
${dir}/../kubernetes/configurer/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin
