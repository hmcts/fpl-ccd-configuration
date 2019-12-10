#!/bin/sh

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../../..)
resources_dir=${root_dir}/service/src/main/resources

function get_user_ids() {
  docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%$1%';" openidm
}

swansea_user_ids=$(echo $(get_user_ids swansea) | tr -d ' ')
hillingdon_user_ids=$(echo $(get_user_ids hillingdon) | tr -d ' ')
swindon_user_ids=$(echo $(get_user_ids swindon) | tr -d ' ')
wiltshire_user_ids=$(echo $(get_user_ids wiltshire) | tr -d ' ')

cat > ${resources_dir}/application-user-mappings.yaml <<EOL
spring:
  profiles: user-mappings

fpl:
  local_authority_user:
    mapping: 'FPLA=>0;SA=>${swansea_user_ids};HN=>${hillingdon_user_ids};SN=>${swindon_user_ids};SNW=>${wiltshire_user_ids}'
EOL
