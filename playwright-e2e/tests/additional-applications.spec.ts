import { test } from '../fixtures/create-fixture';
import { testConfig } from '../settings/test-config';
import {CTSCTeamLeadUser, CTSCUser, HighCourtAdminUser, newSwanseaLocalAuthorityUserOne } from '../settings/user-credentials';
import { Apihelp } from '../utils/api-helper';
import caseData from '../caseData/mandatorySubmissionFields.json';
import { expect } from '@playwright/test';
import { setHighCourt } from '../utils/update-case-details';
import { AdditionalApplications } from '../pages/additional-applications';

test.describe('Additional Applications', () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
      caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

test('Failed Payment High Court WA task', async ({ page, signInPage, additionalApplications, caseFileView }) => {
    casename = 'High Court Review Correspondence WA task ' + dateTime.slice(0, 10);
    setHighCourt(caseData);
    await apiDataSetup.updateCase(casename, caseNumber, caseData);
    await signInPage.visit();
    await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
    await signInPage.navigateTOCaseDetails(caseNumber);
    await additionalApplications.uploadBasicC2Application();
    

    // Check CFV
    
    await caseFileView.goToCFVTab();
    await caseFileView.openFolder('Application');
    await caseFileView.openFolder('C2 applications');
    await expect(page.getByRole('tree')).toContainText('textfile.pdf');

    // If WA is enabled
    if (testConfig.waEnabled) {
        console.log('WA testing');
        await additionalApplications.clickSignOut();
        await signInPage.visit();
        await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);

        await signInPage.navigateTOCaseDetails(caseNumber);

        // Judge in Wales should see this Welsh case task + be able to assign it to themselves
        await additionalApplications.tabNavigation('Tasks');
        await additionalApplications.waitForTask('Failed Payment (High Court)');

        // Assign and complete the task
        await page.getByText('Assign to me').click();
        await page.getByText('Mark as done').click();
        await page.getByRole('button', { name: "Mark as done" }).click();

        // Should be no more tasks on the page
        await expect(page.getByText('Failed Payment (High Court)')).toHaveCount(0);
    }

});

});