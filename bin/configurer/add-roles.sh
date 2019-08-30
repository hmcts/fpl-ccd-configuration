#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/idam-add-role.sh "ccd-import"
${dir}/utils/idam-add-role.sh "caseworker"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw"

${dir}/utils/ccd-add-role.sh caseworker-publiclaw-solicitor
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-solicitor"
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-courtadmin"
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-cafcass
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-cafcass"
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-gatekeeper
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-gatekeeper"
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-systemupdate
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-systemupdate"
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-judiciary
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-judiciary"
