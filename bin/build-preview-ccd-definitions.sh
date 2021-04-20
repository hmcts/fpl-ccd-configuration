#!/bin/bash

set -eu

root_dir=$(realpath $(dirname ${0})/..)
config_dir=${root_dir}/ccd-definition
build_dir=${root_dir}/build/ccd-release-config

mkdir -p ${build_dir}

# build the ccd definition file
export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-pr-${1}.service.core-compute-preview.internal
export CCD_DEF_AAC_URL=http://aac-fpl-case-service-pr-${1}.service.core-compute-preview.internal

${root_dir}/fpla-docker/bin/utils/fpl-process-definition.sh ${config_dir} ${build_dir}/ccd-fpl-preview-${1}-toggle-on.xlsx "-e *-prod.json"
${root_dir}/fpla-docker/bin/utils/fpl-process-definition.sh ${config_dir} ${build_dir}/ccd-fpl-preview-${1}-toggle-off.xlsx "-e *-nonprod.json"
