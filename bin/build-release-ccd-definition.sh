#!/bin/bash

set -eu

environment=${1:-prod}

if [[ ${environment} != "prod" && ${environment} != "aat" && ${environment} != "saat" && ${environment} != "demo" && ${environment} != "ithc" ]]; then
  echo "Environment '${environment}' is not supported!"
  exit 1
fi

if [ ${environment} == "prod" ]; then
  excludedFilenamePatterns="-e UserProfile.json,*-nonprod.json"
else
  excludedFilenamePatterns="-e UserProfile.json"
fi

root_dir=$(realpath $(dirname ${0})/..)
config_dir=${root_dir}/ccd-definition
build_dir=${root_dir}/build/ccd-release-config
release_definition_output_file=${build_dir}/ccd-fpl-${environment}.xlsx

mkdir -p ${build_dir}

# build the ccd definition file
export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-${environment}.service.core-compute-${environment}.internal
${root_dir}/bin/configurer/utils/fpl-process-definition.sh ${config_dir} ${release_definition_output_file} "${excludedFilenamePatterns}"
