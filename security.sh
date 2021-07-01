#!/usr/bin/env bash
#setting encoding for Python 2 / 3 compatibilities
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export PYTHONDONTWRITEBYTECODE=1
zap-api-scan.py -t ${URL_FOR_SECURITY_SCAN}/v2/api-docs -f openapi -S -d -u ${SECURITY_RULES} -P 1001 -l FAIL --hook=zap_hooks.py
cat zap.out
curl --fail http://0.0.0.0:1001/OTHER/core/other/jsonreport/?formMethod=GET --output report.json

echo "LC_ALL: ${LC_ALL}"
echo "LANG: ${LANG}"
echo "PYTHONDONTWRITEBYTECODE: ${PYTHONDONTWRITEBYTECODE}"
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o api-report.html -f html
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l Informational --exit-code False
cp report.json functional-output/
cp api-report.html functional-output/
