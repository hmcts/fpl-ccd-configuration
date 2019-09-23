#!/bin/sh

set -eu

echo user ids script

DATABASE=openidm
USERNAME=openidm
HOSTNAME=localhost
export PGPASSWORD=openidm

userids=$(psql -U $USERNAME -d $DATABASE -h $HOSTNAME -p 5051 -t -c "SELECT fullobject-> 'userName' as userName, fullobject-> '_id' as id FROM managedobjects WHERE fullobject->>'sn' = '(local-authority)';")

echo user ids are: $userids

