#!/usr/bin/env bash

set -eu

name=${1}
description=${2}

docker run --rm --network ${DATABASE_NETWORK:-host} postgres:11-alpine psql --host ${DATABASE_HOST} --username postgres --command "INSERT INTO role(name, display_name) VALUES('${name}', '${description}') ON CONFLICT DO NOTHING" idam
