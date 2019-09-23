#!/bin/sh

set -eu

echo user ids script

export PGPASSWORD=openidm

psql -U openidm -d openidm -h localhost -p 5051 -c "SELECT objectid FROM openidm.managedobjects"

echo finished
