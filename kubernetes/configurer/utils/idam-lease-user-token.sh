#!/bin/bash

id=${1}
role=${2}

curl -k --silent -X POST \
  ${IDAM_API_BASE_URL}/testing-support/lease \
  -Fid=${id} \
  -Frole=${role}
