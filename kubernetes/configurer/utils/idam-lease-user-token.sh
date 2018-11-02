#!/usr/bin/env bash

set -e

id=${1}
role=${2}

curl -k --fail --show-error --silent -X POST \
  ${IDAM_API_BASE_URL}/testing-support/lease \
  -Fid=${id} \
  -Frole=${role}
