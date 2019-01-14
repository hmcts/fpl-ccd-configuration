#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../..)
dev_config_dir=${root_dir}/ccd-definition
build_dir=${root_dir}/build/ccd-development-config
definition_filepath=${build_dir}/ccd_fpl_dev.xlsx

mkdir -p ${build_dir}

${dir}/utils/fpl-process-definition.sh ${dev_config_dir} ${definition_filepath}
${dir}/utils/ccd-import-definition.sh ${definition_filepath}