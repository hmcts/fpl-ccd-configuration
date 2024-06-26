# Playwright End to End suite

End to End testing suite using:

- Playwright https://playwright.dev/
- TypeScript https://www.typescriptlang.org/

## 🤖 Starting up

For all options take a look at https://playwright.dev/docs/running-tests

To execute the 'smoke-test.spec.ts' individually from the Terminal, use the command `yarn playwright test smoke-test.spec.ts`.

## 📁 Structure

```sh
|- playwright-e2e
|-|- fixtures # With fixtures, you can group tests based on their meaning, instead of their common setup.
|-|- pages # Where to keep page classes with respective locators and methods. We utilise POM (Page Object Modeling).
|-|- settings # essential settings for the framework, such as user credentials and URLs.
|-|- tests # Here is where you can do your magic. 🧙‍♂️

 playwright.config.ts # This sits outside playwright-e2e folder, but is the config file for playwright only tests.
 .env # This sits outside playwright-e2e folder, this is required to run your tests locally. See Setup Environment Variables below.
```

## 🔐 Setup Environment Variables

This repository contains automation tests that can be run locally. To set up the environment variables for configuring URLs and passwords, follow the instructions below:

1. Create a .env file in the root directory of this project if it doesn't already exist.

2. Add the following environment variables to the .env file: (ask a team mate for details/values can be found in Azure Keyvault)
   - Can set ENVIRONMENT to use the default URLs for that environment (aat/demo/perftest/ithc) + skip manually defining the following 4

3. Enable WA tests by setting WA_ENABLED to true (if testing on demo, or after WA release)

```
# URLs
ENVIRONMENT=aat
FE_BASE_URL=https://example.com/login
AAT_BASE_URL=https://example.com
IDAM_API_URL=https://example.com
CASE_SERVICE_URL=https://example.com

# userCredentials and Passwords
E2E_TEST_PASSWORD=passwordhere
SYSTEM_UPDATE_USER_USERNAME =systemUserloginEmail
SYSTEM_UPDATE_USER_PASSWORD =systemUserPassword
E2E_TEST_JUDGE_PASSWORD=passwordhere

# Enable WA Tests (disabled by default)
WA_ENABLED=true

# Ports
SERVER_PORT=
```
Replace the placeholder URLs and passwords with the actual values relevant to your environment.

`FE_BASE_URL=` Can be used to toggle between PR environment and AAT or other environments.

3. Save the .env file.

.env file is excluded from version control using Git's .gitignore.

## Install Dependencies

Before running the automation tests, ensure that all necessary dependencies are installed. You can do this by running:

```
yarn install
```

## Running Tests

Once the environment variables are configured and dependencies are installed, you can run the automation tests using the following command:
```
yarn playwright test smoke-test.spec.ts
```

## 🎬 Debugging

Playwright provides a couple of great debugging capabilities at all levels. The ones that you will probably find most useful are:

For all options take a look at https://playwright.dev/docs/debug
