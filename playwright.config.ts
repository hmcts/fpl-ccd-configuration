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
  /* Run tests in files in parallel */
  fullyParallel: true,
  timeout: 3*60*1000, //each test execution time is set to 3 min
  expect: { timeout: 1*110*1000 }, //wait time for the assertion to be true 110 sec

  /* Fail the build on CI if you accidentally left test.only in the source code. */
  forbidOnly: !!process.env.CI,
  /* Retry on CI only */
  retries: process.env.CI ? 2 : 0,
  /*build fails when reaches 35 failed test - fail fast*/
  maxFailures: process.env.CI ? 35 : 0,
  /* Opt out of parallel tests on CI. */
  workers: process.env.CI ? 3 : undefined,
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
          name: 'user-session-setup',
          testMatch: '**/*user-auth-setup.ts',
      },
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"],

      },
        dependencies: ['user-session-setup'],

    },

    {
      name: "firefox",
      use: { ...devices["Desktop Firefox"] },
      dependencies: ['user-session-setup'],
    },

    {
      name: "webkit",
      use: { ...devices["Desktop Safari"] },
      dependencies: ['user-session-setup'],
    },
    {
      name: "preview",
      use: { ...devices["Desktop Firefox"] },
      dependencies: ['user-session-setup'],
      retries: 3,
      timeout: 3*60*1000,
      expect: { timeout: 1*60*1000 },
    },

    /* Test against mobile viewports. */
      {
          name: "ipadPro11",
          use: { ...devices["iPad Pro 11 landscape"] },
          dependencies: ['user-session-setup'],
      },
    {
      name: "GalaxyS4",
      use: { ...devices["Galaxy Tab S4 landscape"] },
        dependencies: ['user-session-setup'],
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
