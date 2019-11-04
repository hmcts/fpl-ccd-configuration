#!/bin/sh

set -eu

function get_user_ids() {
  kubectl exec service/shared-db -- psql --username openidm --tuples-only --command "SELECT string_agg(fullobject->>'_id',',') FROM managedObjects WHERE fullobject->>'userName' LIKE '%$1%';" openidm
}

swansea_user_ids=$(echo $(get_user_ids swansea) | tr -d ' ')
hillingdon_user_ids=$(echo $(get_user_ids hillingdon) | tr -d ' ')

echo "FPLA=>0;SA=>${swansea_user_ids};HN=>${hillingdon_user_ids}"
