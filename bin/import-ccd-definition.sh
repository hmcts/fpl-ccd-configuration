#!/usr/bin/env bash

set -e

export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://localhost:4502
export IDAM_API_BASE_URL=http://localhost:4501
export CCD_DEFINITION_STORE_API_BASE_URL=http://localhost:4451
export CCD_DEF_CASE_SERVICE_BASE_URL=http://localhost:4000

dir=$(dirname ${0})

${dir}/../kubernetes/configurer/utils/fpl-process-definition.sh
${dir}/../kubernetes/configurer/utils/ccd-import-definition.sh ${dir}/../ccd-definition.xlsx
