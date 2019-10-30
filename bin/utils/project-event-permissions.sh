#!/bin/bash

set -eu

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
  WHERE e.case_type_id = (SELECT MAX(id) FROM case_type)
    AND \"create\" IS TRUE
  ORDER BY s.display_order,
    user_role,
    event_id;
"

docker run -e PGPASSWORD='ccd' --rm --network ccd-network postgres:11-alpine psql --host ccd-shared-database --username ccd --command "${query}" ccd_definition
