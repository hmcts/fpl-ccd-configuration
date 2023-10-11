#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

# Cross jurisdictions caseworkers
roles=("caa" "approver")
for role in "${roles[@]}"
do
  ${dir}/utils/ccd-add-role.sh "caseworker-${role}"
done

${dir}/utils/ccd-add-role.sh "prd-aac-system"

# publiclaw jurisdiction caseworkers
roles=("solicitor" "courtadmin" "cafcass" "magistrate" "gatekeeper" "systemupdate" "judiciary" "bulkscan" "bulkscansystemupdate" "localAuthority" "superuser")
for role in "${roles[@]}"
do
  ${dir}/utils/ccd-add-role.sh "caseworker-publiclaw-${role}"
done

${dir}/utils/ccd-add-role.sh "citizen"
${dir}/utils/ccd-add-role.sh "caseworker-ras-validation"
