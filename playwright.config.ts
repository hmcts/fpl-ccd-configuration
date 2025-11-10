import {defineConfig, devices} from "@playwright/test"
import {ProjectsConfig} from "@hmcts/playwright-common";

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
    testMatch: '*spec.ts',
    testIgnore: '*api.spec.ts',
    /* Run tests in files in parallel */
    fullyParallel: true,
    timeout: 5 * 60 * 1000, //each test execution time is set to 5 min
    expect: {timeout: 1 * 60 * 1000}, //wait time for the assertion to be true 60 sec

    /* Fail the build on CI if you accidentally left test.only in the source code. */
    forbidOnly: !!process.env.CI,
    /* Retry on CI only */
    retries: process.env.CI ? 2 : 0, // 3 tries in total
    /*build fails when reaches 35 failed test - fail fast*/
    maxFailures: process.env.CI ? 30 : 0,
    /* Opt out of parallel tests on CI. */
    workers: process.env.CI ? 4 : undefined,
    /* Reporter to use. See https://playwright.dev/docs/test-reporters */
    reporter: [[process.env.CI ? 'html' : 'list'],
        ['html', {outputFolder: '../test-results/functionalTest'}]],

    /* Shared settings for all the projects below. See https://playwright.dev/docs/api/class-testoptions. */
    use: {
        // Record trace only when retrying a test for the first time.
        trace: 'on-first-retry'

    },

    /* Configure projects for major browsers */
    projects: [
        {
            name: 'AMRoleCleanup',
           testMatch: '/settings/global-teardown.ts',
        },
        {
            ...ProjectsConfig.edge,
            teardown: process.env.CI ? 'AMRoleCleanup' : undefined,

        },
        {
            ...ProjectsConfig.chrome,
            grep: /@xbrowser/
        },
        {
            ...ProjectsConfig.firefox,
            grep: /@xbrowser/
        },
        {
            ...ProjectsConfig.webkit,
            grep: /@xbrowser/
        },
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


        {
            name: "preview",
            use: { ...devices['Desktop Edge'], channel: 'msedge' },
            workers: process.env.CI ? 2 : undefined,
            retries: 2,
            timeout: 5 * 60 * 1000,
            expect: {timeout: 1 * 60 * 1000},
        },

    ],

});
