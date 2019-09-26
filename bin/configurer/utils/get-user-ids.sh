#!/bin/sh

set -eu

echo user ids script

DATABASE=openidm
USERNAME=openidm
HOSTNAME=localhost
export PGPASSWORD=openidm

userids=$(docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') from managedObjects WHERE fullobject->>'sn' = '(local-authority)';" openidm)

useridswithoutwhitespace=$(echo $userids | tr -d ' ')

echo user ids are $useridswithoutwhitespace

sed -i '' "s/31,32,33/$useridswithoutwhitespace/" "../../../service/src/main/resources/application.yaml"
sed -i '' "s/31,32,33/$useridswithoutwhitespace/" "../../../build/resources/main/application.yaml"

echo test script finished

