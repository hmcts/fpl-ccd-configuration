#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../..)

${dir}/utils/fpl-process-definition.sh ${root_dir}/ccd-definition ${root_dir} ccd-definition.xlsx
${dir}/utils/ccd-import-definition.sh ${root_dir}/ccd-definition.xlsx
