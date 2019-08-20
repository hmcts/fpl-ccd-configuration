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

Add users and roles:

```bash
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

To disable retry upon test failure please set `TEST_RETRIES` environment variable to `0`. By default 2 retries are enabled. 

```$bash
TEST_RETRIES=0 yarn test
```

## Service:
See [fpl-service](service/README.md) for more information.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
