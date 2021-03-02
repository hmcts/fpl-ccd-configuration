#!/bin/bash
set -ex

if [[ "$BROWSER_GROUP" == "" ]]
then
    EXIT_STATUS=0
    BROWSER_GROUP=chrome yarn test-crossbrowser-e2e || EXIT_STATUS=$?
    echo EXIT_STATUS: $EXIT_STATUS
    exit $EXIT_STATUS
else
    # Compatible with Jenkins parallel crossbrowser pipeline
    yarn test-crossbrowser-e2e
fi
