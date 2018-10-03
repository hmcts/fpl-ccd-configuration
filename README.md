# fpl-ccd-configuration
Family public law's implementation of the CCD template

### Contents:
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Testing](#testing)

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
To ensure you have the correct dependencies run `npm i` in the command line

## Testing:
To run e2e tests enter `codeceptjs run` in the command line

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.