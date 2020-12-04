#!/usr/bin/env bash

set -eu

main() {
    checkArgs "$@"

    userEmail=${1}
    password=${2}
    idamUrl=${3:-"https://idam-api.platform.hmcts.net"}
    fplUrl=${4:-"http://fpl-case-service-prod.service.core-compute-prod.internal"}

    accessToken=$(getAccessToken ${userEmail} ${password})
    response=$(shareCase ${accessToken})
    echo "Response: ${response}"
}

checkArgs() {
    if [ $# -lt 3 ]
    then
        echo "Usage: shareCase.sh <systemUserEmail> <systemUserPassword> [<idamUrl>] [<fplUrl>] "
        exit 1
    fi
}

getAccessToken() {
    authResponse=$(curl -k --show-error --silent --header 'Content-Type: application/x-www-form-urlencoded' -H 'Accept: application/json' -d "username=${1}&password=${2}" ${idamUrl}/loginUser)
    accessToken=$(echo $authResponse|sed -n 's|.*"access_token":"\([^"]*\)".*|\1|p')
    echo ${accessToken}
}

shareCase() {
    response=$(curl --show-error --silent -w "\n\n%{http_code}" -X POST ${fplUrl}/support/cases/re-share -H "Content-type: application/json" -H "Authorization: Bearer ${1}")
    echo ${response}
}

main "$@"; exit
