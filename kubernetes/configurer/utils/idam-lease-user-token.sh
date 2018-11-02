#!/usr/bin/env bash

set -e

id=${1}
role=${2}

curl -k --fail --silent --write-out "%{url_effective}: %{http_code} response received in %{time_total}s\n" -X POST \
  ${IDAM_API_BASE_URL}/testing-support/lease \
  -Fid=${id} \
  -Frole=${role}
