#!/usr/bin/env bash

branchName=$1

#Checkout specific branch of wa-standalone-task-bpmn
git clone https://github.com/hmcts/wa-standalone-task-bpmn.git

if [ ! -d "./wa-standalone-task-bpmn" ]; then
  exit 1
fi

echo "Switch to ${branchName} branch on wa-standalone-task-bpmn"
cd wa-standalone-task-bpmn
git checkout ${branchName}
cd ..

#Copy bpmn files to camunda folder
if [ ! -d "./camunda" ]; then
  mkdir camunda
fi

cp -r ./wa-standalone-task-bpmn/src/main/resources/*.bpmn ./camunda
rm -rf ./wa-standalone-task-bpmn
