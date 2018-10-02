#!/bin/bash

(ccd-docker/bin/ccd-add-role.sh caseworker-publiclaw-localAuthority)

(ccd-docker/bin/idam-create-caseworker.sh caseworker,caseworker-publiclaw,caseworker-publiclaw-localAuthority local-authority@example.com)
