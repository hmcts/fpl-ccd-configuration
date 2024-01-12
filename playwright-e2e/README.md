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
```

## 🔐 Configuring tests to run locally

The password for the test user accounts has been securely stored in a vault. If you execute tests locally without updating the configuration, the tests will not pass, and you may inadvertently trigger account lockout by repeatedly attempting to log in with an incorrect password.

To prevent this issue, modify a specific setting in:
fpl-ccd-configuration/playwright-e2e/settings/userCredentials.ts

Update the line:
`const e2ePw = process.env.E2E_TEST_PASSWORD || "enter_in_password_for_running_locally";`

Ensure you change the password accordingly. Please be cautious not to include these modifications when submitting your pull request.

## 🎬 Debugging

Playwright provides a couple of great debugging capabilities at all levels. The ones that you will probably find most useful are:

For all options take a look at https://playwright.dev/docs/debug
