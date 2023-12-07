export URL="https://manage-case.aat.platform.hmcts.net"
export IDAM_API_URL="https://idam-api.aat.platform.hmcts.net"
export CASE_SERVICE_URL="http://fpl-case-service-aat.service.core-compute-aat.internal"
export CASE_SERVICE_URL="http://fpl-case-service-aat.service.core-compute-aat.internal"
export PARALLEL_CHUNKS=1
export SHOW_BROWSER_WINDOW=true
export SAUCE_USERNAME=kasi-hmcts
export SAUCE_ACCESS_KEY=9faae091-c553-43fd-a8f3-d8cb4ecf8920

yarn test:singleTest
 #yarn test:crossbrowser-e2e
