# fpl-ccd-configuration
Family public law's implementation of the CCD template

### Contents:
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Service](#service)

## Prerequisites:
Have [Docker](https://www.docker.com) installed.

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
```
sh addUsersAndRoles.sh
```

Add the spreadsheet:
```
./ccd-docker/bin/ccd-import-definition.sh FPL_CCD_DEFINITION_TEMPLATEx.xlsx
```

Refer to [ccd-docker](https://github.com/hmcts/ccd-docker) for more information.

## Getting Started:
To ensure you have the correct dependencies run `yarn install` in the command line.

## Code Style:
To run code linting enter `yarn lint` in the command line.

## Testing:
E2E tests are configured to run in parallel in 3 headless browsers by default.

To run e2e tests enter `yarn test` in the command line.

### Optional configuration

To run all tests only in one browser please set `PARALLEL_CHUNKS` environment variable to `1`.

```$bash
PARALLEL_CHUNKS=1 yarn test
```

To show tests in browser window as they run please set `SHOW_BROWSER_WINDOW` environment variable to `true`.

```$bash
SHOW_BROWSER_WINDOW=true yarn test
```

## Service:
See [fpl-service](service/README.md) for more information.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
