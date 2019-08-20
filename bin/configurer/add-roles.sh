#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

if [[ "${IDAM_VERSION:-tactical}" == 'tactical' ]]; then
  ${dir}/utils/idam-add-role.sh caseworker-publiclaw-gatekeeper "Case Worker Public Law Gatekeeper"
fi

${dir}/utils/ccd-add-role.sh caseworker-publiclaw-solicitor
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-cafcass
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-gatekeeper
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-systemupdate
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-judiciary
