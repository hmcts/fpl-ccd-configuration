#!/bin/bash

username=$1
password=$2
clientId=ccd_gateway
clientSecret=${CCD_API_GATEWAY_IDAM_CLIENT_SECRET:-ccd_gateway_secret}
redirectUri=http://localhost:3451/oauth2redirect

curl --silent --location "${IDAM_API_BASE_URL}/o/token" \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode "client_id=$clientId" \
--data-urlencode "client_secret=$clientSecret" \
--data-urlencode "redirect_uri=$redirectUri" \
--data-urlencode "username=$username" \
--data-urlencode "password=$password" \
--data-urlencode 'scope=openid profile roles' \
--data-urlencode 'grant_type=password' \
| jq -r .access_token
