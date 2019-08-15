#!/usr/bin/env bash

set -eu

pr=${1}

echo 'export IDAM_VERSION=strategic'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=https://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
echo "export IDAM_API_BASE_URL=https://idam-api.aat.platform.hmcts.net"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=https://definition-store-api-fpl-case-service-pr-${pr}.service.core-compute-preview.internal"
echo "export URL=https://case-management-web-fpl-case-service-pr-${pr}.service.core-compute-preview.internal"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-pr-${pr}.service.core-compute-preview.internal"
