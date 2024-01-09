# Playwright End to End suite

End to End testing suite using:

- Playwright https://playwright.dev/
- TypeScript https://www.typescriptlang.org/

## 🤖 Starting up

For all options take a look at https://playwright.dev/docs/running-tests

To execute the 'smoke-test.spec.ts' individually from the Terminal, use the command `yarn playwright test smoke-test.spec.ts`

## 📁 Structure

```sh
|- playwright-e2e
|-|- local-config # config.js where you can store local configuration e.g. default password.
|-|- pages # Where to keep page classes with respective locators and methods.
|-|- tests # Here is where you can do your magic. 🧙‍♂️
|-|- settings # essential settings for the framework, such as user credentials and URLs.

 playwright.config.ts # This sits outside playwright-e2e folder, but is the config file for playwright only tests.
```

## 🔐 Configuring Default Password

Create file 'config.js' under folder playwright-e2e/local-config.

// config.js
`export const DefaultPassword = "your_default_password_here";`

Add the default password for users, this will save you having to edit any code to run tests locally while you are developing.

Also check that config.js is added to gitignore.

## 🎬 Debugging

Playwright provides a couple of great debugging capabilities at all levels. The ones that you will probably find most useful are:

For all options take a look at https://playwright.dev/docs/debug
