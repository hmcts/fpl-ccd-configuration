#!/usr/bin/env bash

set -eu

pr=${1}

echo 'export ENVIRONMENT=preview'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-demo.service.core-compute-demo.internal"
echo "export IDAM_API_BASE_URL=https://idam-api.demo.platform.hmcts.net"
echo "export CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-demo.service.core-compute-demo.internal/oauth2redirect"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=https://ccd-definition-store-fpl-case-service-pr-${pr}.service.core-compute-preview.internal"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-pr-${pr}.service.core-compute-preview.internal"
