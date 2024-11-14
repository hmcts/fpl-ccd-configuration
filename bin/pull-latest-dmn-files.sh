#!/usr/bin/env bash

branchName=$1

#Checkout specific branch pf  camunda bpmn definition
git clone https://github.com/hmcts/fpl-wa-task-configuration.git
cd fpl-wa-task-configuration

echo "Switch to ${branchName} branch on fpl-wa-task-configuration"
git checkout ${branchName}
cd ..

#Copy camunda folder which contains dmn files
cp -r ./fpl-wa-task-configuration/src/main/resources .
rm -rf ./fpl-wa-task-configuration

./bin/import-dmn-diagram.sh . publiclaw fpl
