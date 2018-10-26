#!/usr/bin/env bash

dir=$(dirname ${0})

${dir}/../ccd-docker/bin/ccd-add-role.sh caseworker-publiclaw-localAuthority
${dir}/../ccd-docker/bin/ccd-add-role.sh caseworker-publiclaw-courtadmin

${dir}/../ccd-docker/bin/idam-create-caseworker.sh caseworker,caseworker-publiclaw,caseworker-publiclaw-localAuthority local-authority@example.com
${dir}/../ccd-docker/bin/idam-create-caseworker.sh caseworker,caseworker-publiclaw,caseworker-publiclaw-courtadmin hmcts-admin@example.com
