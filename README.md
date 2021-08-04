# fpl-ccd-configuration
Family public law's implementation of the CCD template

### Contents:
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Service](#service)

## Prerequisites:

- [Docker](https://www.docker.com)
- [realpath-osx](https://github.com/harto/realpath-osx) (Mac OS only)
- [jq](https://stedolan.github.io/jq/)

Run command:
```
git submodule init
git submodule update
```

Add services, roles and users from fpla-docker repository.

Run
```
./bin/generate-local-user-mappings.sh
```
script each time new LA users are added in order to fix access segregation locally.

Load CCD definition:

CCD definition is stored in JSON format. To load it into CCD instance please run:

```bash
$ ./bin/import-ccd-definition.sh
```

Note: Above script will export JSON content into XLSX file and upload it into instance of CCD definition store.

Additional note:

You can skip some of the files by using -e option on the import-ccd-definitions, i.e.

```bash
$ ./bin/import-ccd-definition.sh -e 'UserProfile.json,*-nonprod.json'
```

The command above will skip UserProfile.json and all files with -nonprod suffix (from the folders).

## Getting Started:
To ensure you have the correct dependencies run `yarn install` in the command line.

## Code Style:
To run code linting enter `yarn lint` in the command line.

## Docmosis Tornado:

Some of the functionality requires Docmosis Tornado to be started.

It requires `DOCMOSIS_KEY` to be exposed as environment variable on your machine.

Docker-compose runs FPL Service as well, refer the  [service README](service/README.md)
for additional explanation what's required to get the FPL service started by Docker Compose.

## Testing:
E2E tests are configured to run in parallel in 3 headless browsers by default.

To run e2e tests enter `yarn test` in the command line.

### Optional configuration

To run all tests only in one browser please set `PARALLEL_CHUNKS` environment variable to `1`. By default 3 chunks are enabled.

```$bash
PARALLEL_CHUNKS=1 yarn test
```

To show tests in browser window as they run please set `SHOW_BROWSER_WINDOW` environment variable to `true`. By default browser window is hidden.

```$bash
SHOW_BROWSER_WINDOW=true yarn test
```

To enable retry upon test failure please set `TEST_RETRIES` environment variable to desired positive value. By default no retries are enabled.

```$bash
TEST_RETRIES=2 yarn test
```

To disable chrome web security

```$bash
DISABLE_SECURITY=true yarn test
```

## Creating sample case via E2E tests

E2E tests can be used to create sample case with mandatory sections only. To do so please run the following command:

```$bash
PARALLEL_CHUNKS=1 yarn test --grep '@create-case-with-mandatory-sections-only'
```

Note: Case number will be printed to the console while tests run e.g. `Application draft #1571-7550-7484-8512 has been created`.

## Running E2E against remote environment
```$bash
URL="https://manage-case.aat.platform.hmcts.net" IDAM_API_URL="https://idam-api.aat.platform.hmcts.net" CASE_SERVICE_URL="http://fpl-case-service-aat.service.core-compute-aat.internal" DM_STORE_URL="http://dm-store-aat.service.core-compute-aat.internal" yarn test
```
If environment requires user to login into hmcts account first then set HMCTS_USER_USERNAME and HMCTS_USER_PASSWORD

## Running E2E against PR enviroment

```$bash
PR=<PR_NUMBER> PARALLEL_CHUNKS=1 SHOW_BROWSER_WINDOW=TRUE URL=http://xui-fpl-case-service-pr-$PR.service.core-compute-preview.internal IDAM_API_URL="https://idam-api.aat.platform.hmcts.net" CASE_SERVICE_URL=http://fpl-case-service-pr-$PR.service.core-compute-preview.internal DM_STORE_URL="http://dm-store-aat.service.core-compute-aat.internal" yarn test
```

## Running api tests

Application must be up and running

```$bash
./gradlew runApiTest
```

## Running email template integration tests locally

In order to run the template tests locally you need to add the gov.uk.notify test-key here:
```
Create the file in this location (it's already included in .gitignore)
.service/src/integrationTest/resources/application-email-template-test.yaml

spring:
  profiles: email-template-test

integration-test:
  notify-service:
    key: <ask for the test key to the other developers>

(the key will start with integrationtests-*, hence not sending real emails since this is a test key,
see https://docs.notifications.service.gov.uk/java.html#api-keys)
```

Report is generated in build/reports/serenity

## Connecting to PR database:

```$bash
kubectl port-forward fpl-case-service-pr-<PR-ID>-postgresql-0 5020:5432
```
then connect to data-store db on port 5020


## Connecting to local open idm database:

```$bash
host: localhost
port: 5051
user: openidm
password: openidm
database: openidm
```
User details are kept in openidm.managedobjects table

## Connecting to PR elastic search:
```$bash
kubectl port-forward fpl-case-service-pr-<PR-ID>-es-master-0 9210:9200
```
then
```$bash
curl http://localhost:9210/care_supervision_epo_cases-000001/_search
```

## Uploading ccd definition into PR environment
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
fpla-docker/bin/utils/ccd-import-definition.sh <FILEPATH>
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

## Service:
See [fpl-service](service/README.md) for more information.

## Stubbing
Some external dependencies need to be stubbed (i.e. professional reference data).
Stubbing is configured in fpla-docker repository

## App insight (optional)
To connect local environment to azure app insight:
- set APPINSIGHTS_INSTRUMENTATIONKEY env variable (value can be found in env vault under name AppInsightsInstrumentationKey)
- add env variable JAVA_TOOL_OPTIONS=-javaagent:<PATH_TO_PROJECT>/fpl-ccd-configuration/lib/applicationinsights-agent-2.6.1.jar

To connect preview env to azure app insight:
- add AppInsightsInstrumentationKey under java.keyVaults.fpla.secrets in charts/fpl-case-service/values.preview.template.yaml

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.



