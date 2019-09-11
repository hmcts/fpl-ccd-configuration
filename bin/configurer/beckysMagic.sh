#!/usr/bin/env bash

set -eu

dir=$(dirname ${0})

${dir}/add-services.sh
${dir}/add-roles.sh
${dir}/add-users.sh
${dir}/import-ccd-definition.sh

echo "Beckys magical script"