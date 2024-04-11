import { test } from '../fixtures/create-fixture';
import { newSwanseaLocalAuthorityUserOne, CTSCTeamLeadUser, judgeWalesUser } from '../settings/user-credentials';
import { Apihelp } from '../utils/api-helper';
import caseData from '../caseData/caseWithDraftOrder.json';
import { expect } from '@playwright/test';
import { testConfig } from '../settings/test-config';

test.describe('Approve Orders', () => {
  let apiDataSetup = new Apihelp();
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let casename: string;
  test.beforeEach(async () => {
    caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });

  test('Review standard CMO ', async ({ page, signInPage, approveOrders, caseFileView }) => {
    casename = 'Review standard CMO ' + dateTime.slice(0, 10);
    await apiDataSetup.updateCase(casename, caseNumber, caseData);
    await signInPage.visit();
    await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
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
      await approveOrders.waitForTask('Review Order', 'urgent');

      // Assign and complete the task
      await page.getByText('Assign to me').click();
      await page.getByText('Mark as done').click();
      await page.getByRole('button', { name: "Mark as done" }).click();

      // Should be no more tasks on the page
      await expect(page.getByText('Review Order')).toHaveCount(0);
    }
  });

});