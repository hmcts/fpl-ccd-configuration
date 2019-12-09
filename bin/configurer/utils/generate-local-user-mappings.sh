#!/bin/sh

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../../..)

mock_file=${root_dir}/docker/wiremock/__files/mockResponse.json
mock_tmp_file=${root_dir}/docker/wiremock/__files/mockResponse.tmp.json
users_file=${root_dir}/bin/configurer/users.json
users_ids_tmp_file=${dir}/userIds.json.tmp

user_mappings_file=${root_dir}/service/src/main/resources/application-user-mappings.yaml

function query_db() {
  docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "$1" openidm
}

function get_all_users_email_id_mappings() {
  query_db "SELECT json_object(array_agg(fullobject->>'mail'), array_agg(fullobject->>'_id')) FROM managedObjects WHERE fullobject->>'mail' IS NOT NULL;"
}

function get_users_ids_by_name_like() {
  query_db "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%$1%';"
}

cp $users_file $mock_file
echo $(get_all_users_email_id_mappings) >> $users_ids_tmp_file

for i in `seq 1 $(jq '. | length' $mock_file)`
do
  user_id=$(jq -r --arg email $(jq -r --argjson index $i '.[$index-1].email' $mock_file) '.[$email]' $users_ids_tmp_file)
  jq -r --argjson index $i --arg user_id $user_id 'if $user_id=="null" then .[$index-1].userIdentifier="" else .[$index-1].userIdentifier=$user_id end | .[$index-1].firstName=.[$index-1].email | .[$index-1].idamStatus="ACTIVE" | .[$index-1].idamStatusCode="0" | .[$index-1].idamMessage="" | .[$index-1].roles|=split(",")' $mock_file > $mock_tmp_file && mv $mock_tmp_file $mock_file
done

rm $users_ids_tmp_file

hillingdon_user_ids=$(echo $(get_users_ids_by_name_like hillingdon) | tr -d ' ')
swindon_user_ids=$(echo $(get_users_ids_by_name_like swindon) | tr -d ' ')
wiltshire_user_ids=$(echo $(get_users_ids_by_name_like wiltshire) | tr -d ' ')

cat > $user_mappings_file <<EOL
spring:
  profiles: user-mappings

fpl:
  local_authority_user:
    mapping: 'FPLA=>0;HN=>${hillingdon_user_ids};SN=>${swindon_user_ids};SNW=>${wiltshire_user_ids}'
EOL
