import { test } from '../fixtures/create-fixture';
import { judgeLondonUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser, CTSCTeamLeadUser, judgeWalesUser } from '../settings/user-credentials';
import { Apihelp } from '../utils/api-helper';
import caseData from '../caseData/caseWithDraftOrder.json';
import { expect } from '@playwright/test';
import { setHighCourt } from '../utils/update-case-details';
import { testConfig } from '../settings/test-config';

test.describe('Approve Orders', () => {
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let casename: string;
  test.beforeEach(async () => {
    caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('Review CMO (High Court) WA Task',
    async ({ page, signInPage, approveOrders, caseFileView }) => {
      casename = 'Review CMO (High Court) WA Task ' + dateTime.slice(0, 10);
      setHighCourt(caseData);
      await apiDataSetup.updateCase(casename, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(judgeLondonUser.email, judgeLondonUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await approveOrders.gotoNextStep('Approve orders')
      await approveOrders.approveNonUrgentDraftCMO();

      //Check CFV
      await caseFileView.goToCFVTab();
      await caseFileView.openFolder('Orders');
      await expect(page.getByRole('tree')).toContainText('draftOrder.pdf');
      
      if (testConfig.waEnabled) {
        await approveOrders.clickSignOut();

        await signInPage.visit();
        await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await approveOrders.tabNavigation('Tasks');
        await approveOrders.waitForTask('Review Order (High Court)');

        // Assign and complete the task
        await page.getByText('Assign to me').click();
        await page.getByText('Mark as done').click();
        await page.getByRole('button', { name: "Mark as done" }).click();

        // Should be no more tasks on the page
        await expect(page.getByText('Review Order (High Court)')).toHaveCount(0);
      }
    });

    test('Review urgent CMO ', async ({ page, signInPage, approveOrders, caseFileView }) => {
      casename = 'Review urgent CMO ' + dateTime.slice(0, 10);
      await apiDataSetup.updateCase(casename, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);
      await approveOrders.gotoNextStep('Approve orders')
      await approveOrders.approveUrgentDraftCMO();

      //Check CFV
      await caseFileView.goToCFVTab();
      await caseFileView.openFolder('Orders');
      await expect(page.getByRole('tree')).toContainText('draftOrder.pdf');

      if (testConfig.waEnabled) {
        await approveOrders.clickSignOut();

        await signInPage.visit();
        await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await approveOrders.tabNavigation('Tasks');
        await approveOrders.waitForTask('Review Order');

        // Assign and complete the task
        await page.getByText('Assign to me').click();
        await page.getByText('Mark as done').click();
        await page.getByRole('button', { name: "Mark as done" }).click();

        // Should be no more tasks on the page
        await expect(page.getByText('Review Order')).toHaveCount(0);
      }
    });

});