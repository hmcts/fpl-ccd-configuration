#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/utils/fpl-process-definition.sh
${dir}/utils/ccd-import-definition.sh ${dir}/../../ccd-definition.xlsx
