#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../..)
build_dir=${root_dir}/build/ccd-development-config
mkdir -p ${build_dir}

${dir}/utils/fpl-process-definition.sh ${root_dir}/ccd-definition ${build_dir} ccd-definition.xlsx
${dir}/utils/ccd-import-definition.sh ${build_dir}/ccd-definition.xlsx