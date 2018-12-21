#!/usr/bin/env bash

set -eu

export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://localhost:4502
export IDAM_API_BASE_URL=http://localhost:4501
export CCD_DEFINITION_STORE_API_BASE_URL=http://localhost:4451
export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-service:4000

export CCD_CONFIGURER_S2S_SECRET=AAAAAAAAAAAAAAAC

$(dirname ${0})/../kubernetes/configurer/import-ccd-definition.sh
