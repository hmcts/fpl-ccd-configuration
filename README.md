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
To run e2e tests enter `codeceptjs run` in the command line

## Service:
See [fpl-serivice](#service/README.md) for more information.

To run e2e tests enter `yarn test` in the command line.
