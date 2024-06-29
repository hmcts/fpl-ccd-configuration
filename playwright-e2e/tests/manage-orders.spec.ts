import {test} from '../fixtures/create-fixture';
import {CTSCUser, newSwanseaLocalAuthorityUserOne,} from "../settings/user-credentials";
import caseData from '../caseData/caseWithHearingDetails.json' assert {type: 'json'};
import {expect} from "@playwright/test";
import {createCase, updateCase} from "../utils/api-helper";

test.describe('manage orders', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });
    test('@local EPO order',
        async ({page, signInPage, orders}) => {
            caseName = 'EPO order ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await orders.gotoNextStep('Manage orders');
            await orders.createorder();
            await orders.clickContinue();
            await orders.selectOrder('Emergency protection order (C23)');
            await orders.clickContinue();
            // await page.pause();
            // await expect(page.getByText('Emergency protection order (C23)', {exact: true})).toBeVisible();
            await expect(page.getByText(' Add issuing details', {exact: true})).toBeVisible();
            await orders.addIssuingDetails();
            await orders.clickContinue();

            //add children involved
            await expect(page.getByRole('heading', {name: 'Add children\'s details', exact: true})).toBeVisible();
            await orders.addChildDetails();
            await orders.clickContinue();

            //add order details
            await orders.addOrderDetails();
            await orders.clickContinue();

            // check the order privew
            await expect(page.getByRole('heading', {name: 'Check your order', exact: true})).toBeVisible();
            await orders.previewOrderBeforeSubmit();
            await expect(orders.orderPage.getByText('Timothy Jones', {exact: true})).toBeVisible();
            await expect(orders.orderPage.getByText('John Black', {exact: true})).toBeVisible();
            await expect(orders.orderPage.getByText('Sarah Black', {exact: true})).toBeVisible();
            await expect(orders.orderPage.getByText('This order ends on 2 October 2013 at 10:00am.', {exact: true})).toBeVisible();
            await orders.clickContinue();

            //check your answer and submit
            await orders.checkYourAnsAndSubmit();

            await orders.tabNavigation('Orders')
            await expect(page.getByRole('cell', {name: 'Emergency protection order (C23)', exact: true})).toBeVisible();
            await expect(page.getByText('Timothy Jones, John Black, Sarah Black', {exact: true})).toBeVisible();

        });

});
