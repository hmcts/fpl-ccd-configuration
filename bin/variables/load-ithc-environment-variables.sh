#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=ithc'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-ithc.service.core-compute-ithc.internal"
echo "export IDAM_API_BASE_URL=https://idam-api.ithc.platform.hmcts.net"
echo "export CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-ithc.service.core-compute-ithc.internal/oauth2redirect"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=http://ccd-definition-store-api-ithc.service.core-compute-ithc.internal"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-ithc.service.core-compute-ithc.internal"
