#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../..)
dev_config_dir=${root_dir}/ccd-definition
build_dir=${root_dir}/build/ccd-development-config
definition_output_file=${build_dir}/ccd-fpl-dev.xlsx

mkdir -p ${build_dir}

${dir}/utils/fpl-process-definition.sh ${dev_config_dir} ${definition_output_file}
${dir}/utils/ccd-import-definition.sh ${definition_output_file}
