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

Load CCD definition:

CCD definition is stored in JSON format. To load it into CCD instance please run: 

```bash
$ ./bin/configurer/import-ccd-definition.sh
```

Note: Above script will export JSON content into XLSX file and upload it into instance of CCD definition store.

## Getting Started:
To ensure you have the correct dependencies run `yarn install` in the command line.

## Code Style:
To run code linting enter `yarn lint` in the command line.

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

## Service:
See [fpl-service](service/README.md) for more information.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
