#!/bin/bash

set -eu

case_type_clause="WHERE reference = 'CARE_SUPERVISION_EPO'"
state_where_clause=""
role_where_clause=""

while getopts "c::s:r:h" option; do
  case ${option} in
    c)
      case_type_clause="WHERE reference = '${OPTARG}'"
      ;;
    s)
      state_where_clause="AND s.reference = '${OPTARG}'"
      ;;
    r)
      role_where_clause="AND r.reference = '${OPTARG}'"
      ;;
    h)
      echo "
        Usage: ${0} [-c case type] [-s case state] [-r user role]

        Options:
          -c: allows to select case type (optional, defaults to 'CARE_SUPERVISION_EPO')
          -s: allows to narrow down the list to specific case state (optional)
          -r: allows to narrow down the list to specific user role (optional)
      "
      exit 1
      ;;
    *)
      ;;
  esac
done

query="
  SELECT COALESCE(s.reference, '*') as state,
    r.reference as user_role,
    e.reference as event_id,
    e.name as event_name
  FROM event e
    LEFT JOIN event_pre_state eps ON e.id = eps.event_id
    LEFT JOIN state s ON eps.state_id = s.id
    JOIN event_acl ea ON e.id = ea.event_id
    JOIN role r ON ea.role_id = r.id
  WHERE e.case_type_id = (SELECT MAX(id) FROM case_type ${case_type_clause})
    AND \"create\" IS TRUE
    ${state_where_clause}
    ${role_where_clause}
  ORDER BY s.display_order,
    user_role,
    event_id;
"

docker run -e PGPASSWORD='ccd' --rm --network ccd-network postgres:11-alpine psql --host ccd-shared-database --username ccd --command "${query}" ccd_definition
