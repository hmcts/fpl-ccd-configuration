#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/..)

user_mappings_file=${root_dir}/service/src/main/resources/application-user-mappings.yaml

function query_db() {
  docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "$1" openidm
}

function get_users_ids() {
  query_db "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%$1%';"
}

hillingdon_user_ids=$(echo $(get_users_ids hillingdon) | tr -d ' ')
swindon_user_ids=$(echo $(get_users_ids swindon) | tr -d ' ')
wiltshire_user_ids=$(echo $(get_users_ids wiltshire) | tr -d ' ')

cat > $user_mappings_file <<EOL
spring:
  profiles: user-mappings

fpl:
  local_authority_user:
    mapping: 'FPLA=>0;HN=>${hillingdon_user_ids};SN=>${swindon_user_ids};SNW=>${wiltshire_user_ids}'
EOL
