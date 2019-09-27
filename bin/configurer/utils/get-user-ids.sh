#!/bin/sh

set -eu

userids=$(docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') from managedObjects WHERE fullobject->>'sn' = '(local-authority)';" openidm)

useridsformatted=$(echo $userids | tr -d ' ')

sed -i '' "s/31,32,33/$useridsformatted/" "../../../service/src/main/resources/application-user-mappings.yaml"
