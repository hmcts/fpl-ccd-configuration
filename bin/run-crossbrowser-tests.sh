#!/bin/bash
set -ex

EXIT_STATUS=0
yarn test:crossbrowser-chrome || EXIT_STATUS=$?
yarn test:crossbrowser-firefox || EXIT_STATUS=$?
yarn test:crossbrowser-safari || EXIT_STATUS=$?
yarn test:crossbrowser-microsoft || EXIT_STATUS=$?
echo EXIT_STATUS: $EXIT_STATUS
exit $EXIT_STATUS
