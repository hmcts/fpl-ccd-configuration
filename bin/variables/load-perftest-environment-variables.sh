#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=perftest'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-perftest.service.core-compute-perftest.internal"
echo "export IDAM_API_BASE_URL=https://idam-api.perftest.platform.hmcts.net"
echo "export CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-perftest.service.core-compute-perftest.internal/oauth2redirect"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=https://ccd-definition-store-api-perftest.service.core-compute-perftest.internal"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-perftest.service.core-compute-perftest.internal"

