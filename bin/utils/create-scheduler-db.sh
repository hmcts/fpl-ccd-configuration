#!/usr/bin/env bash

docker exec -it compose_ccd-shared-database_1 psql -U postgres -c "CREATE USER fpl_scheduler WITH PASSWORD 'fpl_scheduler'"
docker exec -it compose_ccd-shared-database_1 psql -U postgres -c "CREATE DATABASE fpl_scheduler WITH OWNER fpl_scheduler"
