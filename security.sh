#!/usr/bin/env bash

#setting encoding for Python 2 / 3 compatibilities
export LC_ALL=C.UTF-8
export LANG=C.UTF-8
export PYTHONDONTWRITEBYTECODE=1
echo "Run ZAP scan and generate reports"
zap-api-scan.py -t ${URL_FOR_SECURITY_SCAN}/v2/api-docs -f openapi -S -d -u ${SECURITY_RULES} -P 1001 -l FAIL --hook=zap_hooks.py -J report.json -r api-report.html
echo "Print alerts"
zap-cli --zap-url http://0.0.0.0 -p 1001 alerts -l Informational --exit-code False

echo "LC_ALL: ${LC_ALL}"
echo "LANG: ${LANG}"
echo "PYTHONDONTWRITEBYTECODE: ${PYTHONDONTWRITEBYTECODE}"
echo "Print zap.out logs:"
cat zap.out

echo "Copy artifacts for archiving"
cp zap.out functional-output/
cp report.json functional-output/
cp api-report.html functional-output/
