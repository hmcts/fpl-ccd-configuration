#!/bin/bash
set -eu

root_dir=$(realpath $(dirname ${0})/..)
dev_config_dir=${root_dir}/ccd-definition
build_dir=${root_dir}/build/ccd-release-config
prod_config_dir=${build_dir}/definitions
prod_definition_output_file=${build_dir}/ccd-fpl-prod.xlsx

# create build folder, copy development config and remove the users
mkdir -p ${prod_config_dir}
cp -a ${dev_config_dir}/. ${prod_config_dir}
rm ${prod_config_dir}/UserProfile.json

# build the ccd definition file
export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-prod.service.core-compute-prod.internal
${root_dir}/kubernetes/configurer/utils/fpl-process-definition.sh ${prod_config_dir} ${prod_definition_output_file}
