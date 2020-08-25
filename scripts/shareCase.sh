#!/usr/bin/env bash

set -eu

main() {
    checkArgs "$@"

    user=${1}
    password=${2}
    caseId=${3}
    commaSeparatedUsersIds=${4}
    idamUrl=${5:-"https://idam-api.platform.hmcts.net"}
    fplUrl=${6:-"http://fpl-case-service-prod.service.core-compute-prod.internal"}

    requestBody=$(buildPayload ${commaSeparatedUsersIds})
    echo "Request payload: ${requestBody}"

    accessToken=$(getAccessToken ${user} ${password})
    response=$(shareCase ${caseId} ${accessToken})
    echo "Response: ${response}"
}

checkArgs() {
    if [ $# -lt 4 ]
    then
        echo "Usage: shareCase.sh <systemUserId> <systemUserPassword> <caseId> <comma separated list of userIds> [<idamUrl>] [<fplUrl>] "
        exit 1
    fi
}

buildPayload() {
    usersIdsArray=(${1//,/ })
    usersIds=$(printf ",\"%s\"" "${usersIdsArray[@]//\'/}")
    usersIds=${usersIds:1}
    requestBody="{\"ids\":[${usersIds}]}"
    echo ${requestBody}
}

getAccessToken() {
    authResponse=$(curl -k --show-error --silent --header 'Content-Type: application/x-www-form-urlencoded' -H 'Accept: application/json' -d "username=${1}&password=${2}" ${idamUrl}/loginUser)
    accessToken=$(echo $authResponse|sed -n 's|.*"access_token":"\([^"]*\)".*|\1|p')
    echo ${accessToken}
}

shareCase() {
    response=$(curl --show-error --silent -w "\n\n%{http_code}" -d "$requestBody" -X POST ${fplUrl}/support/case/${1}/share -H "Content-type: application/json" -H "Authorization: Bearer ${2}")
    echo ${response}
}

main "$@"; exit
