#!/usr/bin/env bash

definition_input_dir=$(realpath 'ccd-definition')
definition_output_file="$(realpath ".")/build/ccd-development-config/ccd-fpl-dev.xlsx"
params="$@"
params=${params:='-e *-prod.json,*-shuttered.json'}
./fpla-docker/bin/import-ccd-definition.sh "${definition_input_dir}" "${definition_output_file}" "${params}"
