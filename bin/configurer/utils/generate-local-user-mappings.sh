#!/bin/sh

set -eu

dir=$(dirname ${0})
root_dir=$(realpath ${dir}/../../..)

mock_user_by_email_template=${root_dir}/docker/wiremock/templates/userByEmail.template.json
mock_user_by_email_dir=${root_dir}/docker/wiremock/mappings/user-by-email

user_template_file=${root_dir}/docker/wiremock/__files/userTemplate.json
mock_file=${root_dir}/docker/wiremock/__files/mockResponse.json
mock_tmp_file=${root_dir}/docker/wiremock/__files/mockResponse.tmp.json
users_file=${root_dir}/bin/configurer/users.json
users_ids_tmp_file=${dir}/userIds.json.tmp

user_mappings_file=${root_dir}/service/src/main/resources/application-user-mappings.yaml

function query_db() {
  docker run -e PGPASSWORD='openidm' --rm --network ccd-network postgres:11-alpine psql --host shared-db  --username openidm --tuples-only  --command "$1" openidm
}

function get_users_email_id_mappings() {
  query_db "SELECT json_object(array_agg(fullobject->>'mail'), array_agg(fullobject->>'_id')) FROM managedObjects WHERE fullobject->>'mail' IS NOT NULL AND fullobject->>'userName' LIKE '%$1%';"
}

function get_users_ids() {
  query_db "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%$1%';"
}

function create_mock_response() {
  jq --arg organisation $1 --argjson template "$(<$user_template_file)" '[.[] | select(.email | contains($organisation)) | $template + .]' $users_file > $mock_file
  echo $(get_users_email_id_mappings $1) > $users_ids_tmp_file

  for i in `seq 0 $(jq '. | length - 1' $mock_file)`
  do
    email=$(jq -r --argjson i $i '.[$i].email' $mock_file)
    user_id=$(jq -r --arg email $email '.[$email]' $users_ids_tmp_file)
    jq -r --argjson i $i --arg user_id $user_id '.[$i].userIdentifier=$user_id | .[$i].firstName=.[$i].email | .[$i].roles|=split(",")' $mock_file > $mock_tmp_file && mv $mock_tmp_file $mock_file
    generate_user_by_email_mock $user_id $email
  done

  rm $users_ids_tmp_file
}

function create_user_by_email_responses() {
  echo $(get_users_email_id_mappings '@') > $users_ids_tmp_file

  for i in `seq 0 $(jq '. | length - 1' $users_file)`
  do
    email=$(jq -r --argjson i $i '.[$i].email' $users_file)
    user_id=$(jq -r --arg email $email '.[$email]' $users_ids_tmp_file)
    generate_user_by_email_mock $user_id $email
  done

  allUsers=$(jq ". | {users: .}" $mock_file)
  echo $allUsers | jq > $mock_file

  rm $users_ids_tmp_file
}

function generate_user_by_email_mock() {
  id=$1
  email=$2

echo $id $email
  if [ "$id" != "null" ]; then
   userByEmailMappings=$(sed -e "s|\[email]|$email|" -e "s|\[userId]|$id|" $mock_user_by_email_template)
   echo $userByEmailMappings | jq '.' > "$mock_user_by_email_dir/${email}.json"
  fi
}


rm -rf $mock_user_by_email_dir
mkdir $mock_user_by_email_dir

create_mock_response swansea
create_user_by_email_responses
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
