#!/bin/bash
set -eu

build_dir=$(realpath $(dirname ${0})/../build/release-config)
prod_config_dir=$build_dir/definitions
dev_config_dir=$(dirname ${0})/../ccd-definition
prod_definition_filename=CCD_FPL_PROD.xlsx

#create build folder, copy development config and remove the users
mkdir -p $prod_config_dir
cp -a $dev_config_dir/. $prod_config_dir
rm $prod_config_dir/UserProfile.json

#build the ccd definition file
touch ${build_dir}/${prod_definition_filename}
export CCD_DEF_CASE_SERVICE_BASE_URL=http://fpl-case-service-prod.service.core-compute-prod.internal
docker run --rm --name json2xlsx \
  -v ${prod_config_dir}:/tmp/ccd-definition\
  -v ${build_dir}/${prod_definition_filename}:/tmp/${prod_definition_filename} \
  -e CCD_DEF_CASE_SERVICE_BASE_URL \
  docker.artifactory.reform.hmcts.net/ccd/ccd-definition-processor:c480382 \
  json2xlsx -D /tmp/ccd-definition -o /tmp/${prod_definition_filename}