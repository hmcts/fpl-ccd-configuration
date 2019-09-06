#!/bin/bash

set -eu

environment=${1:-prod}

if [[ ${environment} != "prod" && ${environment} != "aat" && ${environment} != "saat" && ${environment} != "demo" && ${environment} != "perftest" ]]; then
  echo "Environment '${environment}' is not supported!"
  exit 1
fi

root_dir=$(realpath $(dirname ${0})/..)
dev_config_dir=${root_dir}/ccd-definition
build_dir=${root_dir}/build/ccd-release-config
release_config_dir=${build_dir}/definitions
release_definition_output_file=${build_dir}/ccd-fpl-${environment}.xlsx

# create build folder, copy development config and remove the users
mkdir -p ${release_config_dir}
cp -a ${dev_config_dir}/. ${release_config_dir}
rm ${release_config_dir}/UserProfile.json

# build the ccd definition file
export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-${environment}.service.core-compute-${environment}.internal
${root_dir}/bin/configurer/utils/fpl-process-definition.sh ${release_config_dir} ${release_definition_output_file}
