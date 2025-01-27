#!/usr/bin/env bash

branchName=$1

#Checkout specific branch of fpl-wa-task-configuration
git clone https://github.com/hmcts/fpl-wa-task-configuration.git

if [ ! -d "./fpl-wa-task-configuration" ]; then
  exit 1
fi

echo "Switch to ${branchName} branch on fpl-wa-task-configuration"
cd fpl-wa-task-configuration
git checkout ${branchName}
cd ..

#Copy dmn files to camunda folder
if [ ! -d "./camunda" ]; then
  mkdir camunda
fi

cp -r ./fpl-wa-task-configuration/src/main/resources/*.dmn ./camunda
rm -rf ./fpl-wa-task-configuration
