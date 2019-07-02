#!/usr/bin/env bash

set -eu

export IDAM_API_BASE_URL=http://localhost:4501

$(dirname ${0})/../kubernetes/configurer/add-oauth-clients.sh
