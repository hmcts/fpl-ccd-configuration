#!/bin/sh

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../../..)
resources_dir=${root_dir}/service/src/main/resources

function GetUserIds()
{
    user_ids=$(docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%$1%';" openidm)
}

GetUserIds swansea
swansea_user_ids_formatted=$(echo $user_ids | tr -d ' ')

GetUserIds hillingdon
hillingdon_user_ids_formatted=$(echo $user_ids | tr -d ' ')

cd $resources_dir
cat > application-user-mappings.yaml <<EOL
spring:
  profiles: user-mappings

fpl:
  local_authority_user:
    mapping: 'FPLA=>0;SA=>${swansea_user_ids_formatted};HN=>${hillingdon_user_ids_formatted}'
EOL



