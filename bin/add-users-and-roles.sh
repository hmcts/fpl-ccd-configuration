#!/usr/bin/env bash

dir=$(dirname ${0})

${dir}/../ccd-docker/bin/ccd-add-role.sh caseworker-publiclaw-localAuthority

${dir}/../ccd-docker/bin/idam-create-caseworker.sh caseworker,caseworker-publiclaw,caseworker-publiclaw-localAuthority local-authority@example.com
