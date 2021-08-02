#!/usr/bin/env bash

function callZapCli {
  counter=0
  until [ $counter -gt 3 ] || zap-cli --zap-url http://0.0.0.0 -p 1001 $1
  do
    ((counter++))
    echo "Retrying... (retry ${counter})"
  done
}

#setting encoding for Python 2 / 3 compatibilities
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export PYTHONDONTWRITEBYTECODE=1
echo "Run ZAP scan"
zap-api-scan.py -t ${URL_FOR_SECURITY_SCAN}/v2/api-docs -f openapi -S -d -u ${SECURITY_RULES} -P 1001 -l FAIL --hook=zap_hooks.py
echo "Generate report.json"
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json
echo "Print zap.out logs"
cat zap.out

echo "LC_ALL: ${LC_ALL}"
echo "LANG: ${LANG}"
echo "PYTHONDONTWRITEBYTECODE: ${PYTHONDONTWRITEBYTECODE}"
echo "Generate api-report.html"
callZapCli "report -o api-report.html -f html"
echo "Retrieve alerts"
callZapCli "alerts -l Informational --exit-code False"
echo "Print zap.out logs"
cat zap.out
echo "Copy artifacts for archiving"
cp report.json functional-output/
cp api-report.html functional-output/
