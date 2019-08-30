#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

# Defaults are ok and create require ccd_gateway service in idam
${dir}/utils/idam-create-service.sh ccd_gateway false ccd_gateway ccd_gateway_secret [\"http://localhost:3451/oauth2redirect\"]

${dir}/utils/idam-create-service.sh fpl false fpl OOOOOOOOOOOOOOOO [\"https://localhost:9000/oauth2/callback\"]

