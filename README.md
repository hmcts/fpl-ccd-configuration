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

Run command:
```
git submodule init
git submodule update
```

You must run this command once to enable modules.
```
./ccd enable backend frontend sidam sidam-local sidam-local-ccd dm-store
```

Creating and starting containers:
```
./ccd compose up -d
```

Add services, roles and users (needs to be run in below order).

Make sure the `IDAM_ADMIN_USER` and `IDAM_ADMIN_PASSWORD` env variables are set to IDAM initial user.
You can run the below scripts by prefixing them with `IDAM_ADMIN_USER= IDAM_ADMIN_PASSWORD=`, 
by exporting variables or use some other approach of managing env variables, 
e.g. [direnv](https://direnv.net).  

The values can be found on [Confluence](https://tools.hmcts.net/confluence/x/eQP3P).

```bash
$ ./bin/configurer/add-services.sh
$ ./bin/configurer/add-roles.sh
$ ./bin/configurer/add-users.sh
```

Users are defined in `bin/configurer/users.json`. Run the final script each time new LA users are added in order to fix access segregation locally.

Load CCD definition:

CCD definition is stored in JSON format. To load it into CCD instance please run: 

```bash
$ ./bin/configurer/import-ccd-definition.sh
```

Note: Above script will export JSON content into XLSX file and upload it into instance of CCD definition store.

Additional note:

You can skip some of the files by using -e option on the import-ccd-definitions, i.e.

```bash
$ ./bin/configurer/import-ccd-definition.sh -e 'UserProfile.json,*-nonprod.json
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

## Creating sample case via E2E tests

E2E tests can be used to create sample case with mandatory sections only. To do so please run the following command:

```$bash
PARALLEL_CHUNKS=1 yarn test --grep '@create-case-with-mandatory-sections-only'
```

Note: Case number will be printed to the console while tests run e.g. `Application draft #1571-7550-7484-8512 has been created`.

## Service:
See [fpl-service](service/README.md) for more information.

## Stubbing 
Some external dependencies need to be stubbed (i.e. professional reference data). 

Docker-compose configures Wiremock to be exposed under port 8765.

docker/wiremock folder configures the stubs themselves. 
Refer to the [documentation](http://wiremock.org)
for an additional guide. 
   

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
