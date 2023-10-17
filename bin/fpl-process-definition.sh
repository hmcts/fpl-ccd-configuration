#!/usr/bin/env bash

set -eu

definition_processor_version=latest

definition_dir=${1}
definition_output_file=${2}
additionalParameters=${3-}

definition_input_dir=${definition_dir}

[[ ! -d /tmp/jenkins-agent ]] && mkdir -p /tmp/jenkins-agent
definition_tmp=$(mktemp -d /tmp/jenkins-agent/fpla.XXXXXX)
definition_tmp_dir="$definition_tmp/ccd-definition"
mkdir -p "$definition_tmp_dir"
cp -a ${definition_input_dir}/* "$definition_tmp_dir"
definition_tmp_out_dir="${definition_tmp_dir}/build/ccd-development-config"
[[ ! -d "$definition_tmp_out_dir" ]] && mkdir -p "$definition_tmp_out_dir"

definition_tmp_out_file="${definition_tmp_out_dir}/ccd-fpl-dev.xlsx"
[[ ! -e "$definition_tmp_out_file" ]] && touch "$definition_tmp_out_file"

docker run --rm --name json2xlsx \
  -v ${definition_tmp_dir}:/tmp/ccd-definition \
  -v ${definition_tmp_out_file}:/tmp/ccd-definition.xlsx \
  -e CCD_DEF_CASE_SERVICE_BASE_URL=${CCD_DEF_CASE_SERVICE_BASE_URL:-http://docker.for.mac.localhost:4000} \
  -e CCD_DEF_AAC_URL=${CCD_DEF_AAC_URL:-http://manage-case-assignment:4454}\
  hmctspublic.azurecr.io/ccd/definition-processor:${definition_processor_version} \
  json2xlsx -D /tmp/ccd-definition -o /tmp/ccd-definition.xlsx ${additionalParameters}

cp "$definition_tmp_out_file"  "$definition_output_file"
