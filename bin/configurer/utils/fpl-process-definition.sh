#!/usr/bin/env bash

set -eu

definition_dir=${1}
definition_output_file=${2}
additionalParameters=${3-}

definition_input_dir=${definition_dir}

if [[ ! -e ${definition_output_file} ]]; then
   touch ${definition_output_file}
fi

docker run --rm --name json2xlsx \
  -v ${definition_input_dir}:/tmp/ccd-definition \
  -v ${definition_output_file}:/tmp/ccd-definition.xlsx \
  -e CCD_DEF_CASE_SERVICE_BASE_URL=${CCD_DEF_CASE_SERVICE_BASE_URL:-http://docker.for.mac.localhost:4000} \
  hmctspublic.azurecr.io/ccd/definition-processor:db1fph \
  json2xlsx -D /tmp/ccd-definition -o /tmp/ccd-definition.xlsx ${additionalParameters}
