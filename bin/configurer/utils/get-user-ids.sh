#!/bin/sh

set -eu

echo user ids script

DATABASE=openidm
USERNAME=openidm
HOSTNAME=localhost
export PGPASSWORD=openidm

userids=$(docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT fullobject->'userName',fullobject->'_id',fullobject->'sn' from managedObjects WHERE fullobject->>'sn' = 'Tester';" openidm)

echo user ids are: $userids

