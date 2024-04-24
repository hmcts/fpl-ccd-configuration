import { test } from '../fixtures/create-fixture';
import { Apihelp } from '../utils/api-helper';
import caseDataByLa from '../caseData/approveOrders/caseWithConfidentialDraftOrderByLa.json';
import caseDataByCtsc from '../caseData/approveOrders/caseWithConfidentialDraftOrderByCtsc.json';
import { newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser, judgeUser } from '../settings/user-credentials';
import { expect } from "@playwright/test";
import { testConfig } from '../settings/test-config';

test.describe('Approve Orders', () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;

    test.beforeEach(async () => {
        caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('Judge approve a confidential order uploaded by LA',
        async ({ page, signInPage, approveOrders }) => {
            casename = 'LA uploads an other application ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseDataByLa);
            await signInPage.visit();
            await signInPage.login(judgeUser.email, judgeUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await approveOrders.navigateToPageViaNextStep();
            await approveOrders.approveOrders();

            await approveOrders.tabNavigation('Orders');
            await expect(page.getByText('Confidential order uploaded by LA')).toBeVisible();

            // LA able to view the approved order
            await approveOrders.clickSignOut();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await approveOrders.tabNavigation('Orders');
            await expect(page.getByText('Confidential order uploaded by LA')).toBeVisible();
        });


    test('Judge approve a confidential order uploaded by CTSC',
        async ({ page, signInPage, approveOrders }) => {
            casename = 'LA uploads an other application ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseDataByCtsc);
            await signInPage.visit();
            await signInPage.login(judgeUser.email, judgeUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await approveOrders.navigateToPageViaNextStep();
            await approveOrders.approveOrders();

            await approveOrders.tabNavigation('Orders');
            await expect(page.getByText('Confidential order uploaded by CTSC')).toBeVisible();

            // CTSC able to view the approved order
            await approveOrders.clickSignOut();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await approveOrders.tabNavigation('Orders');
            await expect(page.getByText('Confidential order uploaded by CTSC')).toBeVisible();

            // LA cannot view the approved order
            await approveOrders.clickSignOut();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await approveOrders.tabNavigation('Orders');
            await expect(page.getByText('This is a confidential draft order and restricted viewing applies')).toBeVisible();
        });
});
