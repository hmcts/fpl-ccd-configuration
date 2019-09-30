#!/bin/sh

set -eu

swanseauserids=$(docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%swansea%';" openidm)
hillingdonuserids=$(docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%hillingdon%';" openidm)

swanseauseridsformatted=$(echo $swanseauserids | tr -d ' ')
hillingdonuseridsformatted=$(echo $hillingdonuserids | tr -d ' ')

cd ../../../service/src/main/resources
cat > application-user-mappings.yaml <<EOL
spring:
  profiles: user-mappings

fpl:
  local_authority_user:
    mapping: 'FPLA=>0;SA=>${swanseauseridsformatted};HN=>${hillingdonuseridsformatted}'
EOL
