#!/bin/bash

dir=$(dirname ${0})

${dir}/utils/idam-create-caseworker.sh local-authority@example.com caseworker,caseworker-publiclaw,caseworker-publiclaw-localAuthority
${dir}/utils/ccd-add-role.sh caseworker-publiclaw-localAuthority

${dir}/utils/fpl-process-definition.sh
${dir}/utils/ccd-import-definition.sh ${dir}/../../ccd-definition.xlsx
