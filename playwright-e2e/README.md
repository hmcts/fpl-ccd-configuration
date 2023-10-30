# Playwright End to End suite

End to End testing suite using:

- Playwright https://playwright.dev/
- TypeScript https://www.typescriptlang.org/

## 🤖 Starting up

For all options take a look at https://playwright.dev/docs/running-tests

## 📁 Structure

```sh
 |- playwright-e2e
 |  |- pages # Where to keep page classes with respective locators and methods.
 |  |- resources # Contains any addtional resources required by the tests, such as images, videos, or audio files.
 |  |   |- config # Configuration file/s for the automation framework, such as settings, test data parameters etc.
 |  |   |- reports # Output reports of test runs, including logs, screenshots and metrics.
 |  |   |- testdata # Test data in various formats such as JSON, XML or CSV.
 |- tests # Here is where you can do your magic. 🧙‍♂️
 |- utils # Sets of pages for our applications.

 playwright.config.ts # This sits outside playwright-e2e folder, but is the config file for playwright only tests.
```

## 🎬 Debugging

Playwright provides a couple of great debugging capabilities at all levels. The ones that you will probably find most useful are:

For all options take a look at https://playwright.dev/docs/debug
