#!/usr/bin/env bash

set -eu

definition_dir=${1}
output_dir=${2}
definition_output_filename=${3}

definition_input_dir=${definition_dir}
definition_output_file=${output_dir}/${definition_output_filename}

if [[ ! -e ${definition_output_file} ]]; then
   touch ${definition_output_file}
fi

docker run --rm --name json2xlsx \
  -v ${definition_input_dir}:/tmp/ccd-definition \
  -v ${definition_output_file}:/tmp/${definition_output_filename} \
  -e CCD_DEF_CASE_SERVICE_BASE_URL \
  docker.artifactory.reform.hmcts.net/ccd/ccd-definition-processor:c480382 \
  json2xlsx -D /tmp/ccd-definition -o /tmp/${definition_output_filename}
