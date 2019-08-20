#!/usr/bin/env bash

set -eu

name=${1}
description=${2}

docker run --rm --network ${DATABASE_NETWORK:-compose_default} postgres:11-alpine psql --host ${DATABASE_HOST:-ccd-shared-database} --username postgres --command "INSERT INTO role(name, display_name) VALUES('${name}', '${description}') ON CONFLICT DO NOTHING" idam
