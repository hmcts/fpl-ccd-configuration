#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

# Defaults are ok and create require ccd_gateway service in idam
#${dir}/utils/create-ccd-service.sh

# citizen / solicitor created by default
${dir}/utils/idam-add-role.sh "ccd-import"
${dir}/utils/idam-add-role.sh "caseworker"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-solicitor"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-courtadmin"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-cafcass"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-gatekeeper"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-systemupdate"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw-judiciary"

${dir}/utils/idam-create-caseworker.sh ccd.docker.default@hmcts.net ccd-import CCD_Docker
