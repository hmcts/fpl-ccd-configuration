import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/caseSentToGatekeeper.json' assert { type: "json" };
import caseWithEpo from '../caseData/caseWithEPO.json' assert { type: "json" };
import {
    newSwanseaLocalAuthorityUserOne,
    HighCourtAdminUser,
    judgeLondonUser,
    CTSCUser
} from "../settings/user-credentials";
import { expect } from "@playwright/test";
import { testConfig } from "../settings/test-config";
import { setHighCourt } from '../utils/update-case-details';

test.describe('Gatekeeping Listing', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;
  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('Review Standard Direction Order High Court WA Task',
    async ({ page, signInPage, gateKeepingListing,
      caseFileView }) => {
      caseName = 'Review Standard Direction Order High Court WA Task ' + dateTime.slice(0, 10);
      setHighCourt(caseData);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(judgeLondonUser.email, judgeLondonUser.password)
      await signInPage.navigateTOCaseDetails(caseNumber);

      await gateKeepingListing.gotoNextStep('Judicial Gatekeeping');
      await gateKeepingListing.completeJudicialGatekeepingWithUploadedOrder();
      await gateKeepingListing.gotoNextStep('List Gatekeeping Hearing');
      await gateKeepingListing.addHighCourtJudgeAndCompleteGatekeepingListing();

       //assert
        await gateKeepingListing.tabNavigation('People in the case');
        await expect.soft(gateKeepingListing.page.getByText('Fee paid judge')).toBeVisible();
        await expect.soft(gateKeepingListing.page.getByText('Recorder')).toBeVisible();
        await expect.soft(gateKeepingListing.page.getByText('Ramirez KC',{ exact: true })).toBeVisible();
        await gateKeepingListing.tabNavigation('Hearings');
        await expect.soft(gateKeepingListing.page.getByText('Recorder Ramirez KC',{exact:true})).toHaveCount(2);
        //Check CFV
      await caseFileView.goToCFVTab();
      await caseFileView.openFolder('Orders');
      await expect(page.getByRole('tree')).toContainText('testWordDoc.pdf');
      await gateKeepingListing.clickSignOut();

      if (testConfig.waEnabled) {
        //Test WA Task exists
        await signInPage.visit();
        await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await gateKeepingListing.tabNavigation('Tasks');
        await gateKeepingListing.waitForTask('Review Standard Direction Order (High Court)');

        // Assign and complete the task
        await page.getByText('Assign to me').click();
        await page.getByText('Mark as done').click();
        await page.getByRole('button', { name: "Mark as done" }).click();

        // Should be no more tasks on the page
        await expect(page.getByText('Review Standard Direction Order (High Court)')).toHaveCount(0);
      }
    });
    test('List Urgent Direction Order - CTS User',
        async ({page, signInPage, gateKeepingListing}) => {

            caseName = 'List urgent direction order by CTSC user ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseWithEpo);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await gateKeepingListing.gotoNextStep('Add urgent directions');
            await gateKeepingListing.completeUrgentDirectionsOrder();
            await expect.soft(page.getByText('has been updated with event: Add urgent directions')).toBeVisible();
            await gateKeepingListing.tabNavigation('Draft orders');
            await expect(page.getByRole('cell', {name: 'draft-urgent-directions-order'})).toBeVisible();
            await gateKeepingListing.gotoNextStep('List Gatekeeping Hearing');
            await gateKeepingListing.completeUDOListing();
            await expect.soft(page.getByText('has been updated with event: List Gatekeeping Hearing')).toBeVisible();
            await gateKeepingListing.tabNavigation('Orders');
            await expect(page.getByRole('cell', {name: 'urgent-directions-order.pdf'}).locator('div').nth(1)).toBeVisible();
            await expect(page.locator('ccd-read-multi-select-list-field').filter({hasText: 'Emergency protection order'}).locator('span')).toBeVisible();
            await expect(page.locator('ccd-field-read-label').filter({hasText: /^Prevent removal from an address$/}).locator('div')).toBeVisible();

        })

  });
