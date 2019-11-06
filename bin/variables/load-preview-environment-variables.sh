#!/usr/bin/env bash

set -eu

pr=${1}

echo 'export ENVIRONMENT=preview'

# urls
echo "export SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
echo "export IDAM_API_BASE_URL=http://api-idam-fpl.service.core-compute-preview.internal"
echo "export CCD_IDAM_REDIRECT_URL=https://case-management-web-fpl-case-service-pr-${pr}.service.core-compute-preview.internal/oauth2redirect"
echo "export CCD_DEFINITION_STORE_API_BASE_URL=http://definition-store-api-fpl-case-service-pr-${pr}.service.core-compute-preview.internal"

# definition placeholders
echo "export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-pr-${pr}.service.core-compute-preview.internal"

# secrets - unset to fallback to defaults
unset CCD_API_GATEWAY_IDAM_CLIENT_SECRET
unset CCD_CONFIGURER_IMPORTER_USERNAME
unset CCD_CONFIGURER_IMPORTER_PASSWORD
