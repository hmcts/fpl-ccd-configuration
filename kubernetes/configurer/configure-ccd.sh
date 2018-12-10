#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/wait-for.sh ${SERVICE_AUTH_PROVIDER_API_BASE_URL} ${IDAM_API_BASE_URL} ${CCD_DEFINITION_STORE_API_BASE_URL} ${CCD_USER_PROFILE_API_BASE_URL}

${dir}/utils/idam-create-caseworker.sh damian@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh kurt@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh james@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh sam@hillingdon.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh siva@hillingdon.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh hmcts-admin@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-courtadmin "(hmcts-admin)"
${dir}/utils/idam-create-caseworker.sh cafcass@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-cafcass "(cafcass)"
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-solicitor
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-cafcass

${dir}/utils/fpl-process-definition.sh
${dir}/utils/ccd-import-definition.sh ${dir}/../../ccd-definition.xlsx
