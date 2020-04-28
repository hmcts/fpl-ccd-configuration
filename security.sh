#!/usr/bin/env bash
echo ${URL}
zap-api-scan.py -t ${URL_FOR_SECURITY_SCAN}/v2/api-docs -f openapi -u ${SECURITY_RULES} -P 1001
cat zap.out
zap-cli --zap-url http://0.0.0.0 -p 1001 report -o /zap/api-report.html -f html
cp /zap/api-report.html functional-output/
zap-cli -p 1001 alerts -l Informational
