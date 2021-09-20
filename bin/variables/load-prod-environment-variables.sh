#!/usr/bin/env bash

set -eu

echo 'export ENVIRONMENT=prod'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-prod.service.core-compute-prod.internal"
echo "export IDAM_API_BASE_URL=https://idam-api.platform.hmcts.net"
echo "export CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-prod.service.core-compute-prod.internal/oauth2redirect"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=http://ccd-definition-store-api-prod.service.core-compute-prod.internal"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-prod.service.core-compute-prod.internal"
