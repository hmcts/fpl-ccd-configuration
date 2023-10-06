#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/idam-add-role.sh "ccd-import"
${dir}/utils/idam-add-role.sh "caseworker"
${dir}/utils/idam-add-role.sh "caseworker-publiclaw"
${dir}/utils/idam-add-role.sh "caseworker-adoption"
${dir}/utils/idam-add-role.sh "pui-case-manager"

# User used during the CCD import and ccd-role creation
${dir}/utils/idam-create-caseworker.sh "ccd.docker.default@hmcts.net" "ccd-import"

# Cross jurisdictions caseworkers
roles=("caa" "approver")
for role in "${roles[@]}"
do
  ${dir}/utils/idam-add-role.sh "caseworker-${role}"
  ${dir}/utils/ccd-add-role.sh "caseworker-${role}"
done

${dir}/utils/idam-add-role.sh "prd-aac-system"
${dir}/utils/ccd-add-role.sh "prd-aac-system"

# publiclaw jurisdiction caseworkers
roles=("solicitor" "courtadmin" "cafcass" "magistrate" "gatekeeper" "systemupdate" "judiciary" "bulkscan" "bulkscansystemupdate" "localAuthority" "superuser")
for role in "${roles[@]}"
do
  ${dir}/utils/idam-add-role.sh "caseworker-publiclaw-${role}"
  ${dir}/utils/ccd-add-role.sh "caseworker-publiclaw-${role}"
done

# adoption jurisdiction caseworkers
roles=("clerk")
for role in "${roles[@]}"
do
  ${dir}/utils/idam-add-role.sh "caseworker-adoption-${role}"
  ${dir}/utils/ccd-add-role.sh "caseworker-adoption-${role}"
done

${dir}/utils/ccd-add-role.sh "citizen"
${dir}/utils/ccd-add-role.sh "caseworker-ras-validation"
