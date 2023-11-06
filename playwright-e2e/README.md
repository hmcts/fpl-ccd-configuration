# Playwright End to End suite

End to End testing suite using:

- Playwright https://playwright.dev/
- TypeScript https://www.typescriptlang.org/

## 🤖 Starting up

run: `yarn install`

## 📁 Structure

```sh
 |- playwright-e2e
 |  |- pages # Where to keep page classes with respective locators and methods.
 |  |- resources # Contains any additional resources required by the tests, such as images, videos, or audio files.
 |  |   |- config # Configuration file/s for the automation framework, such as settings, test data parameters etc.
 |  |   |- reports # Output reports of test runs, including logs, screenshots and metrics.
 |  |   |- testdata # Test data in various formats such as JSON, XML or CSV.
 |- tests # Here is where you can do your magic. 🧙‍♂️
 |- utils # Sets of pages for our applications.

 playwright.config.ts # This sits outside playwright-e2e folder, but is the config file for playwright only tests.
```
## How to run the tests

`npx playwright test --grep @smoke-playwrightonly`

For all options take a look at: https://playwright.dev/docs/running-tests 

## Important 

In playwright.config.ts action timeout has been set globally to `actionTimeout: 65000`.
This is in place of explicit waits which are not healthy for any automation framework.
This is to allow for ExUI loading spinner to finish, it is currently very slow in AAT environment. Without this global action timeout, tests will fail.

## 🎬 Debugging

Playwright provides a couple of great debugging capabilities at all levels. The ones that you will probably find most useful are:

For all options take a look at: https://playwright.dev/docs/debug

## Defining standards

### Define standard naming conventions
We recommend the following:

- Use camelCase for variable and function names.
- Use PascalCase for class names and interface names.
- Use camelCase for interface members.
- Use PascalCase for type names and enum names.
- Name files with camelCase (for example, ebsVolumes.tsx or storage.tsb)