#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/idam-create-caseworker.sh "damian@swansea.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "kurt@swansea.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "james@swansea.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "sam@hillingdon.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "siva@hillingdon.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "raghu@swindon.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "sam@swindon.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "james@swindon.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"

${dir}/utils/idam-create-caseworker.sh "raghu@wiltshire.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "sam@wiltshire.gov.uk" "caseworker,caseworker-publiclaw,caseworker-publiclaw-solicitor" "(local-authority)"
${dir}/utils/idam-create-caseworker.sh "hmcts-admin@example.com" "caseworker,caseworker-publiclaw,caseworker-publiclaw-courtadmin" "(hmcts-admin)"
${dir}/utils/idam-create-caseworker.sh "cafcass@example.com" "caseworker,caseworker-publiclaw,caseworker-publiclaw-cafcass" "(cafcass)"
${dir}/utils/idam-create-caseworker.sh "judiciary@mailnesia.com" ",caseworker-publiclaw-courtadmin, caseworker,caseworker-publiclaw,caseworker-publiclaw-judiciary,caseworker-publiclaw-gatekeeper" "(judiciary)"
${dir}/utils/idam-create-caseworker.sh "gatekeeper@mailnesia.com" "caseworker,caseworker-publiclaw,caseworker-publiclaw-gatekeeper,caseworker-publiclaw-courtadmin" "(gatekeeper)"
${dir}/utils/idam-create-caseworker.sh "fpl-system-update@mailnesia.com" "caseworker,caseworker-publiclaw,caseworker-publiclaw-systemupdate" "(system-update)"
