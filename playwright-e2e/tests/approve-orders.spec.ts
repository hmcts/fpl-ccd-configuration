import { test } from '../fixtures/create-fixture';
import {createCase, updateCase} from "../utils/api-helper";
import { testConfig } from '../settings/test-config';
import caseDataByLa from '../caseData/approveOrders/caseWithConfidentialDraftOrderByLa.json' assert { type: 'json' };
import caseDataByCtsc from '../caseData/approveOrders/caseWithConfidentialDraftOrderByCtsc.json' assert { type: 'json' };
import caseData from '../caseData/caseWithDraftOrder.json' assert { type: "json" };
import { newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser, judgeUser, judgeLondonUser, HighCourtAdminUser } from '../settings/user-credentials';
import { setHighCourt } from '../utils/update-case-details';
import { expect } from "@playwright/test";

test.describe('Approve Orders @sessionreuse', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('Judge approves a confidential order uploaded by LA',
        async ({ page,legalUser, localAuthorityUser,approveOrders }) => {
            casename = 'LA uploads an other application ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseDataByLa);

            await approveOrders.switchUser(legalUser.page)

            await approveOrders.navigateTOCaseDetails(caseNumber);

            await approveOrders.navigateToPageViaNextStep();
            await approveOrders.approveOrders();

            await approveOrders.tabNavigation('Orders');
            await expect(approveOrders.page.getByText('Confidential order uploaded by LA')).toBeVisible();

            // LA able to view the approved order
            await approveOrders.switchUser(localAuthorityUser.page)
            await approveOrders.navigateTOCaseDetails(caseNumber);
            await approveOrders.tabNavigation('Orders');
            await expect(approveOrders.page.getByText('Confidential order uploaded by LA')).toBeVisible();
        });


    test('Judge approve a confidential order uploaded by CTSC',
        async ({ page, legalUser,ctscUser,localAuthorityUser, approveOrders }) => {
            casename = 'LA uploads an other application ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseDataByCtsc);
            await approveOrders.switchUser(legalUser.page)

            await approveOrders.navigateTOCaseDetails(caseNumber);

            await approveOrders.gotoNextStep('Approve orders');
            await approveOrders.approveOrders();

            await approveOrders.tabNavigation('Orders');
            await expect(approveOrders.page.getByText('Confidential order uploaded by CTSC')).toBeVisible();

            // CTSC able to view the approved order
            await approveOrders.switchUser(ctscUser.page)
            await approveOrders.navigateTOCaseDetails(caseNumber);
            await approveOrders.tabNavigation('Orders');
            await expect(approveOrders.page.getByText('Confidential order uploaded by CTSC')).toBeVisible();

            // LA cannot view the approved order
            await approveOrders.switchUser(localAuthorityUser.page);
            await approveOrders.navigateTOCaseDetails(caseNumber);
            await approveOrders.tabNavigation('Orders');
            await expect(approveOrders.page.getByText('Confidential order uploaded by CTSC')).toBeHidden();
        });

    test('Review CMO (High Court) WA Task',
    async ({ page, signInPage, approveOrders, caseFileView }) => {
      casename = 'Review CMO (High Court) WA Task ' + dateTime.slice(0, 10);
      setHighCourt(caseData);
      await updateCase(casename, caseNumber, caseData);
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
});
