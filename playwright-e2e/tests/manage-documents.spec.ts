import { test } from '../fixtures/create-fixture';
import { testConfig } from '../settings/test-config';
import { CTSCUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from '../settings/user-credentials';
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: 'json' };
import { expect } from '@playwright/test';
import { createCase, updateCase } from "../utils/api-helper";
import { setHighCourt } from '../utils/update-case-details';

test.describe('Manage Documents', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('LA uploads correspondence documents', async ({ page, signInPage, manageDocuments, caseFileView }) => {
        caseName = 'LA uploads correspondence documents ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await manageDocuments.uploadDocuments('Court correspondence');

        // Check CFV
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Court Correspondence');
        await expect(page.getByRole('tree')).toContainText('testTextFile.txt');

        // If WA is enabled
        if (testConfig.waEnabled) {
            console.log('WA testing');
            await manageDocuments.clickSignOut();
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);

            await signInPage.navigateTOCaseDetails(caseNumber);

            // Judge in Wales should see this Welsh case task + be able to assign it to themselves
            await manageDocuments.tabNavigation('Tasks');
            await manageDocuments.waitForTask('Review Correspondence');

            // Assign and complete the task
            await page.getByText('Assign to me').click();
            await page.getByText('Mark as done').click();
            await page.getByRole('button', { name: "Mark as done" }).click();

            // Should be no more tasks on the page
            await expect(page.getByText('Review Correspondence')).toHaveCount(0);
        }
    });

    test('High Court Review Correspondence WA task', async ({ page, signInPage, manageDocuments, caseFileView }) => {
    caseName = 'High Court Review Correspondence WA task ' + dateTime.slice(0, 10);
    setHighCourt(caseData);
    await updateCase(caseName, caseNumber, caseData);
    await signInPage.visit();
    await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
    await signInPage.navigateTOCaseDetails(caseNumber);
    await manageDocuments.uploadDocuments('Court correspondence');

    // Check CFV
    await caseFileView.goToCFVTab();
    await caseFileView.openFolder('Court Correspondence');
    await expect(page.getByRole('tree')).toContainText('testTextFile.txt');

    // If WA is enabled
    if (testConfig.waEnabled) {
      console.log('WA testing');
      await manageDocuments.clickSignOut();
      await signInPage.visit();
      await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);

      await signInPage.navigateTOCaseDetails(caseNumber);

      await manageDocuments.tabNavigation('Tasks');
      await manageDocuments.waitForTask('Review Correspondence (High Court)');

      // Assign and complete the task
      await page.getByText('Assign to me').click();
      await page.getByText('Mark as done').click();
      await page.getByRole('button', { name: "Mark as done" }).click();

      // Should be no more tasks on the page
      await expect(page.getByText('Review Correspondence (High Court)')).toHaveCount(0);
    }
  });
});
