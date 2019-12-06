#!/bin/sh

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../../..)
resources_dir=${root_dir}/service/src/main/resources
wiremock_dir=${root_dir}/docker/wiremock
swansea_org_mock_file=${wiremock_dir}/__files/swanseaOrg.json

usernames=(damian kurt james)

function get_user_ids() {
  docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%$1%';" openidm
}

swansea_user_ids=$(echo $(get_user_ids swansea) | tr -d ' ')

echo "[]" > $swansea_org_mock_file
for user_id in ${swansea_user_ids//,/ }
do
  jq --argjson userInfo "$(jq --arg userId "$user_id" '.userIdentifier=$userId' ${wiremock_dir}/templates/userTemplate.json)" '. += [$userInfo]' $swansea_org_mock_file > swanseaOrg.json.tmp && mv swanseaOrg.json.tmp $swansea_org_mock_file
done

for i in `seq 1 $(jq '. | length' $swansea_org_mock_file)`
do
  jq --argjson index $i --arg email "${usernames[$i-1]}@swansea.gov.uk" '.[$index-1].email=$email | .[$index-1].firstName=$email' $swansea_org_mock_file > swanseaOrg.json.tmp && mv swanseaOrg.json.tmp $swansea_org_mock_file
done

hillingdon_user_ids=$(echo $(get_user_ids hillingdon) | tr -d ' ')
swindon_user_ids=$(echo $(get_user_ids swindon) | tr -d ' ')
wiltshire_user_ids=$(echo $(get_user_ids wiltshire) | tr -d ' ')

cat > ${resources_dir}/application-user-mappings.yaml <<EOL
spring:
  profiles: user-mappings

fpl:
  local_authority_user:
    mapping: 'FPLA=>0;HN=>${hillingdon_user_ids};SN=>${swindon_user_ids};SNW=>${wiltshire_user_ids}'
EOL
