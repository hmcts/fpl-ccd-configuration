import { test } from '../fixtures/create-fixture';
import { Apihelp } from '../utils/api-helper';
import caseData from '../caseData/mandatoryWithMultipleChildren.json';
import {newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser} from '../settings/user-credentials';
import { expect } from "@playwright/test";
import { testConfig } from '../settings/test-config';

test.describe('Add a case flag', () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('LA uploads a C1 application',
        async ({ page, signInPage, additionalApplications }) => {
            caseName = 'LA uploads an other application ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await additionalApplications.gotoNextStep('Add case flag');
            await additionalApplications.chooseOtherApplicationType();
            await additionalApplications.fillOtherApplicationDetails();

            // Payment details
            await expect(page.getByText('£232.00')).toBeVisible();
            await additionalApplications.payForApplication();

            await additionalApplications.checkYourAnsAndSubmit();

            await additionalApplications.tabNavigation('Other applications');

            // can see some basic properties of the application
            await expect(page.getByText('PBA1234567')).toBeVisible();
            await expect(page.getByText('C1 - Change surname or remove from jurisdiction')).toBeVisible();
            await expect(page.getByText('On the same day')).toBeVisible();

            // If WA is enabled
            if (testConfig.waEnabled) {
                console.log('WA testing');
                await additionalApplications.clickSignOut();
                await signInPage.visit();
                await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
                await signInPage.navigateTOCaseDetails(caseNumber);

                // Judge in Wales should see this Welsh case task + be able to assign it to themselves
                await additionalApplications.tabNavigation('Tasks');
                await additionalApplications.waitForTask('View Additional Applications');

                // Assign and complete the task
                await page.getByText('Assign to me').click();
                await page.getByText('Mark as done').click();
                await page.getByRole('button', {name: "Mark as done"}).click();

                // Should be no more tasks on the page
                await expect(page.getByText('View Additional Applications')).toHaveCount(0);
            }
        });

});





test('test', async ({ page }) => {
    await page.goto('https://idam-web-public.aat.platform.hmcts.net/login?client_id=xuiwebapp&redirect_uri=https://manage-case.aat.platform.hmcts.net/oauth2/callback&state=y3pKfxpZxxerdBb8BUqO6i_Q0zVrclf_skD8FQOWTrw&nonce=bZq1slgR42XNIKlBCn-nR6bIsc7f3rIUNejU5eLRW1w&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user%20search-user&prompt=');
    await page.getByLabel('Email address').click();
    await page.getByLabel('Email address').fill('fpl-ctsc-admin@justice.gov.uk');
    await page.getByLabel('Password').click();
    await page.getByLabel('Password').fill('Password12');
    await page.getByRole('button', { name: 'Sign in' }).click();
    await page.getByLabel('Email address').click();
    await page.getByLabel('Email address').fill('fpl-ctsc-admin@justice.gov.uk');
    await page.getByLabel('Password').click();
    await page.getByLabel('Password').fill('Password12');
    await page.getByLabel('Password').press('Enter');
    await page.goto('https://manage-case.aat.platform.hmcts.net/cases');
    await page.getByRole('link', { name: 'Case list' }).click();
    await page.getByLabel('Case type').selectOption('10: Object');
    await page.locator('.hmcts-filter-layout__filter').click();
    await page.locator('#dynamicFilters').click();
    await page.getByLabel('Apply filter').click();
    await page.getByLabel('go to case with Case reference:1713-3798-3624-').click();
    await page.getByLabel('Next step').selectOption('24: Object');
    await page.getByRole('button', { name: 'Go' }).click();
    await page.getByLabel('Yes').check();
    await page.getByRole('textbox', { name: 'Upload assessment form or' }).click();
    await page.getByRole('textbox', { name: 'Upload assessment form or' }).setInputFiles('Spot the bugs Session.docx');
    await page.getByLabel('Additional notes (Optional)').click();
    await page.getByLabel('Additional notes (Optional)').fill('additional notes');
    await page.getByRole('button', { name: 'Continue' }).click();
    await page.getByRole('button', { name: 'Save and continue' }).click();
    await page.getByRole('link', { name: 'Add or remove case flag' }).click();
    await page.getByLabel('No', { exact: true }).check();
    await page.getByRole('button', { name: 'Continue' }).click();
    await page.getByRole('button', { name: 'Save and continue' }).click();
});
