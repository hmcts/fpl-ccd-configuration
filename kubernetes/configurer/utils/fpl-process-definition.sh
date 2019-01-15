#!/usr/bin/env bash

set -eu

definition_dir=${1}
definition_output_file=${2}

definition_input_dir=${definition_dir}

if [[ ! -e ${definition_output_file} ]]; then
   touch ${definition_output_file}
fi

docker run --rm --name json2xlsx \
  -v ${definition_input_dir}:/tmp/ccd-definition \
  -v ${definition_output_file}:/tmp/ccd-definition.xlsx \
  -e CCD_DEF_CASE_SERVICE_BASE_URL \
  docker.artifactory.reform.hmcts.net/ccd/ccd-definition-processor:c480382 \
  json2xlsx -D /tmp/ccd-definition -o /tmp/ccd-definition.xlsx
