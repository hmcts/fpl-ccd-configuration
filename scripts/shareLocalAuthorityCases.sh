#!/usr/bin/env bash

set -eu

main() {
    checkArgs "$@"

    user=${1}
    password=${2}
    localAuthority=${3}
    commaSeparatedUsersIds=${4}
    idamUrl=${5:-"https://idam-api.platform.hmcts.net"}
    fplUrl=${6:-"http://fpl-case-service-prod.service.core-compute-prod.internal"}

    requestBody=$(buildPayload ${commaSeparatedUsersIds})
    echo "Request payload: ${requestBody}"

    accessToken=$(getAccessToken ${user} ${password})
    response=$(shareCases ${accessToken})
    echo "Response: ${response}"
}

checkArgs() {
    if [ $# -lt 4 ]
    then
        echo "Usage: shareLocalAuthorityCases.sh <systemUserId> <systemUserPassword> <localAuthority> <comma separated list of userIds> [<idamUrl>] [<fplUrl>] "
        exit 1
    fi
}

buildPayload() {
    usersIdsArray=(${1//,/ })
    usersIds=$(printf ",\"%s\"" "${usersIdsArray[@]//\'/}")
    usersIds=${usersIds:1}
    requestBody="{\"usersIds\":[${usersIds}], \"localAuthority\":\"${localAuthority}\"}"
    echo ${requestBody}
}

getAccessToken() {
    authResponse=$(curl -k --show-error --silent --header 'Content-Type: application/x-www-form-urlencoded' -H 'Accept: application/json' -d "username=${1}&password=${2}" ${idamUrl}/loginUser)
    accessToken=$(echo $authResponse|sed -n 's|.*"access_token":"\([^"]*\)".*|\1|p')
    echo ${accessToken}
}

shareCases() {
    response=$(curl --show-error --silent -w "\n\n%{http_code}" -d "$requestBody" -X POST ${fplUrl}/support/cases/share -H "Content-type: application/json" -H "Authorization: Bearer ${1}")
    echo ${response}
}

main "$@"; exit
