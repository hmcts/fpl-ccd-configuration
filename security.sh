#!/usr/bin/env bash
echo ${URL_FOR_SECURITY_SCAN}
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
zap-api-scan.py -t "${URL_FOR_SECURITY_SCAN}/v2/api-docs" -f openapi -S -s -d -u ${SECURITY_RULES} -P 1001 -l FAIL
cat zap.out
echo "ZAP has successfully started"
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
cp /zap/report.json functional-output/report.json
cp /zap/api-report.html functional-output/zap-security-report.html
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l High --exit-code False
