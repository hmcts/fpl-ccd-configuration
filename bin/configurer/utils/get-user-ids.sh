#!/bin/sh

set -eu

echo user ids script

DATABASE=openidm
USERNAME=openidm
HOSTNAME=localhost
export PGPASSWORD=openidm

userids=$(psql -U $USERNAME -d $DATABASE -h $HOSTNAME -p 5051 -t -c "SELECT objectid FROM managedobjects")

echo user ids are: $userids

