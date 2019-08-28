#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/ccd-add-role.sh caseworker-publiclaw-solicitor
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-courtadmin
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-cafcass
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-gatekeeper
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-systemupdate
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-judiciary
