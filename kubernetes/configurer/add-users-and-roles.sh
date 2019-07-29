#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/idam-add-role.sh caseworker-publiclaw-gatekeeper "Case Worker Public Law Gatekeeper"
${dir}/utils/idam-create-caseworker.sh damian@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh kurt@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh james@swansea.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh sam@hillingdon.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh siva@hillingdon.gov.uk caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor "(local-authority)"
${dir}/utils/idam-create-caseworker.sh hmcts-admin@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-courtadmin "(hmcts-admin)"
${dir}/utils/idam-create-caseworker.sh cafcass@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-cafcass "(cafcass)"
${dir}/utils/idam-create-caseworker.sh gatekeeper@mailnesia.com caseworker,caseworker-publiclaw,caseworker-publiclaw-gatekeeper "(gatekeeper)"
${dir}/utils/idam-create-caseworker.sh judiciary@mailnesia.com caseworker,caseworker-publiclaw,caseworker-publiclaw-judiciary "(judiciary)"
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-solicitor
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-cafcass
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-gatekeeper
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-systemupdate
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-judiciary
