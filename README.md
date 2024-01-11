# fpl-ccd-configuration

Family public law's implementation of the CCD template

## Contents:
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the application](#running-the-application)
- [Testing](#testing)
- [PR environment](#pr-environment)
- [Service](#service)

## Prerequisites

- [Docker](https://www.docker.com)
- [realpath-osx](https://github.com/harto/realpath-osx) (Mac OS only)
- [jq](https://stedolan.github.io/jq/)
- access to Azure key vault
- active VPN
- [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli) (optional for secret management)

## Getting Started

Create the following two files (they are already included in .gitignore)

`service/src/main/resources/application-user-mappings.yaml`:
```
spring:
  profiles: user-mappings
fpl:
  local_authority_user:
    mapping: <get from key vault>
```
and `service/src/main/resources/application-feature-toggle.yaml`:
```
spring:
  config:
    activate:
      on-profile: feature-toggle

ld:
  sdk_key: <get from key vault>
```
(See [fpl-service](service/README.md#custom-flag-values) for more information on Feature Toggle)

To get values from vault login into https://portal.azure.com/ and find related vault and value, or run
```
az login
az keyvault secret show --name <secret_name> --vault-name <vault_name> | grep value
```
(If you can't access the resource try signing in with a different tenant.)

To ensure you have the correct dependencies run `yarn install` in the command line.

## Running the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To run the application use:
```
./gradlew bootWithCCD
```

or from IntelliJ: right-click the bootWithCCD Gradle task and select 'Run...'

then you can access XUI on [http://localhost:3000](http://localhost:3000)

(You may be prompted to run `az login` to set up account.)

### Code Style
To run code linting enter `yarn lint` in the command line.

### Docmosis Tornado

Some functionality requires Docmosis Tornado to be started.
It requires `DOCMOSIS_KEY` to be exposed as environment variable on your machine.

Refer to the confluence page at [https://tools.hmcts.net/confluence/x/QRTgWw](https://tools.hmcts.net/confluence/x/QRTgWw)
for additional explanation.

## Testing E2E

We use Playwright with TypeScript. All the details can be found in the [E2E README.md](./playwright-e2e/README.md).

### Running api tests

Application must be up and running

```$bash
./gradlew runApiTest
```

### Running email template integration tests locally

In order to run the template tests locally you need to add the gov.uk.notify test-key here:

Create the file in this location `service/src/integrationTest/resources/application-email-template-test.yaml`
(it's already included in .gitignore)
```
spring:
  profiles: email-template-test

integration-test:
  notify-service:
    key: <get from key vault>
```
(the key will start with integrationtests-*, hence not sending real emails since this is a test key,
see https://docs.notifications.service.gov.uk/java.html#api-keys)

Report is generated in build/reports/serenity

## PR environment

### Running E2E test against PR environment

To run all E2E tests against a PR
```$bash
PR=<PR_NUMBER>; NODE_TLS_REJECT_UNAUTHORIZED=0 PARALLEL_CHUNKS=1 SHOW_BROWSER_WINDOW=TRUE URL=https://xui-fpl-case-service-pr-$PR.service.core-compute-preview.internal IDAM_API_URL="https://idam-api.aat.platform.hmcts.net" CASE_SERVICE_URL=http://fpl-case-service-pr-$PR.service.core-compute-preview.internal yarn test
```

To run a selected E2E test against a PR
```$bash
PR=<PR_NUMBER>; NODE_TLS_REJECT_UNAUTHORIZED=0 PARALLEL_CHUNKS=1 SHOW_BROWSER_WINDOW=TRUE URL=https://xui-fpl-case-service-pr-$PR.service.core-compute-preview.internal IDAM_API_URL="https://idam-api.aat.platform.hmcts.net" CASE_SERVICE_URL=http://fpl-case-service-pr-$PR.service.core-compute-preview.internal yarn test --grep 'Case administration after submission'
```

### Connecting to PR database

```$bash
kubectl port-forward fpl-case-service-pr-<PR-ID>-postgresql-0 5020:5432
```
then connect to data-store db on port 5020

A list of port numbers is available on [RSE CFT lib](https://github.com/hmcts/rse-cft-lib#ports)

### Connecting to PR elastic search
```$bash
kubectl port-forward fpl-case-service-pr-<PR-ID>-es-master-0 9210:9200
```
then
```$bash
curl http://localhost:9210/care_supervision_epo_cases-000001/_search
```

### Uploading ccd definition into PR environment
On PR env following ccd definition files are generated and stored as jenkins job artefacts:
- ccd-fpl-preview-<PR_ID>-toggle-on.xlsx (uploaded automatically by jenkins)
- ccd-fpl-preview-<PR_ID>-toggle-off.xlsx

you can download these files and import against PR env like follow (vpn needed):


```$bash
PR=<PR_ID> \
CCD_DEFINITION_STORE_API_BASE_URL=https://ccd-definition-store-fpl-case-service-pr-$PR.service.core-compute-preview.internal \
SERVICE_AUTH_PROVIDER_API_BASE_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal \
IDAM_API_BASE_URL=https://idam-api.aat.platform.hmcts.net \
CCD_IDAM_REDIRECT_URL=https://ccd-case-management-web-aat.service.core-compute-aat.internal/oauth2redirect \
CCD_CONFIGURER_IMPORTER_USERNAME=<USER> \
CCD_CONFIGURER_IMPORTER_PASSWORD=<PASSWORD> \
CCD_API_GATEWAY_IDAM_CLIENT_SECRET=<IDAM_CLIENT_SECRET> \
CCD_API_GATEWAY_S2S_SECRET=<S2S_SECRET> \
bin/ccd-import-definition.sh <FILEPATH>
```

where:
- PR_ID - id of pr, file will be uploaded into this PR env
- USER - vault: fpl-aat.ccd-importer-username
- PASSWORD - vault: fpl-aat.ccd-importer-password
- IDAM_CLIENT_SECRET - vault: ccd-aat.ccd-api-gateway-oauth2-client-secret
- S2S_SECRET - vault: s2s-aat.microservicekey-ccd-gw
- FILEPATH - path to file to be uploaded

to get values from vault login into https://portal.azure.com/ and find related vault and value
or
```$bash
az login
az keyvault secret show --name <secret_name> --vault-name <vault_name> | grep value
```

## Service
See [fpl-service](service/README.md) for more information on custom configuration parameters, feature toggle, scheduler.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
