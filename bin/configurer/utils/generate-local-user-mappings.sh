#!/bin/sh

set -eu

SPRING_PROFILES=user-mappings

userids=$(docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') from managedObjects WHERE fullobject->>'sn' = '(local-authority)';" openidm)

useridsformatted=$(echo $userids | tr -d ' ')

cd ../../../service/src/main/resources
cat > application-user-mappings.yaml <<EOL
spring:
  profiles: ${SPRING_PROFILES}

fpl:
  local_authority_user:
    mapping: 'FPLA=>0;SA=>${useridsformatted};HN=>34,35'
EOL
