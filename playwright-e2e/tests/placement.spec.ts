import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/caseInPrepareForHearing.json' assert { type: "json" };
import { CTSCUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";
import { testConfig } from "../settings/test-config";
import { setHighCourt } from '../utils/update-case-details';

test.describe('Placement', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;
  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('Check Placement Application High Court WA Task',
    async ({ page, signInPage, placement,
      caseFileView }) => {
      caseName = 'Placement Application High Court WA Task ' + dateTime.slice(0, 10);
      setHighCourt(caseData);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);

      await placement.gotoNextStep('Placement');
      await placement.submitPlacementOrder();

      //Check CFV
      await caseFileView.goToCFVTab();
      await caseFileView.openFolder('Placement applications and responses');
      await expect(page.getByRole('tree')).toContainText('testPdf.pdf');
      await expect(page.getByRole('tree')).toContainText('testPdf2.pdf');
      await expect(page.getByRole('tree')).toContainText('testPdf3.pdf');
      await caseFileView.openFolder('Confidential');
      await expect(page.getByRole('tree')).toContainText('testPdf4.pdf');
      await placement.clickSignOut();

      if (testConfig.waEnabled) {
        //Test WA Task exists
        await signInPage.visit();
        await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await placement.tabNavigation('Tasks');
        await placement.waitForTask('Check Placement Application (High Court)');

        // Assign and complete the task
        await page.locator('exui-case-task').filter({ hasText: 'Check Placement Application (' }).locator('#action_claim').click();
        await page.getByText('Mark as done').click();
        await page.getByRole('button', { name: "Mark as done" }).click();

        // Should be no more tasks on the page
        await expect(page.getByText('Check Placement Application (High Court)')).toHaveCount(0);
      }
    });
});
