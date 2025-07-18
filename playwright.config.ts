import { defineConfig, devices } from "@playwright/test";

/**
 * Read environment variables from file.
 * https://github.com/motdotla/dotenv
 */
// require('dotenv').config();

/**
 * See https://playwright.dev/docs/test-configuration.
 */
export default defineConfig({

  testDir: "./playwright-e2e",
  testMatch:'*spec.ts',
  testIgnore:'*api.spec.ts',
  /* Run tests in files in parallel */
  fullyParallel: true,
  timeout: 5*60*1000, //each test execution time is set to 5 min
  expect: { timeout: 1*60*1000 }, //wait time for the assertion to be true 60 sec

  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 4 : 0,
  /*build fails when reaches 35 failed test - fail fast*/
  maxFailures: process.env.CI ? 35 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? '50%' : undefined,
  /* Reporter to use. See https://playwright.dev/docs/test-reporters */
  reporter: [[process.env.CI ? 'html' : 'list'],
             ['html', { outputFolder: '../test-results/functionalTest' }]],

  /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
  use: {
    // Record trace only when retrying a test for the first time.
    trace: 'on-first-retry'

  },

  /* Configure projects for major browsers */
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },

    {
      name: "firefox",
      use: { ...devices["Desktop Firefox"] },
       grep: /@xbrowser/, // Run only tests tagged with @xbrowser in Firefox
    },

    {
      name: "webkit",
      use: { ...devices["Desktop Safari"] },
        grep: /@xbrowser/,
    },
    {
      name: "preview",
      use: { ...devices["Desktop Firefox"] },
      retries: 3,
      timeout: 3*60*1000,
      expect: { timeout: 1*60*1000 },
    },

    /* Test against mobile viewports. */
      {
          name: "ipadPro11",
          use: { ...devices["iPad Pro 11 landscape"] },
          grep: /@xbrowser/,
      },
    {
      name: "GalaxyS4",
      use: { ...devices["Galaxy Tab S4 landscape"] },
      grep: /@xbrowser/,
    },


    /* Test against branded browsers. */
    // {
    //   name: 'Microsoft Edge',
    //   use: { ...devices['Desktop Edge'], channel: 'msedge' },
    // },
    // {
    //   name: 'Google Chrome',
    //   use: { ...devices['Desktop Chrome'], channel: 'chrome' },
    // },
  ],

  /* Run your local dev server before starting the tests */
  // webServer: {
  //   command: 'npm run start',
  //   url: 'http://127.0.0.1:3000',
  //   reuseExistingServer: !process.env.CI,
  // },
});
